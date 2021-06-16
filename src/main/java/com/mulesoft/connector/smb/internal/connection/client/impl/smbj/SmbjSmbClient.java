/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.client.impl.smbj;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mserref.NtStatus;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.security.bc.BCSecurityProvider;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.api.SmbFileAttributes;
import com.mulesoft.connector.smb.internal.connection.client.impl.AbstractSmbClient;
import com.mulesoft.connector.smb.internal.utils.SmbUtils;
import org.mule.extension.file.common.api.FileWriteMode;
import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.util.IOUtils;

import java.io.*;
import java.net.URI;
import java.util.*;

import static org.mule.extension.file.common.api.exceptions.FileError.*;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

public class SmbjSmbClient extends AbstractSmbClient {


  private DiskShare share;
  private HashMap<NtStatus, FileError> errorMap = new HashMap<>();

  {
    this.errorMap.put(NtStatus.STATUS_CONNECTION_DISCONNECTED, DISCONNECTED);
    this.errorMap.put(NtStatus.STATUS_LOGON_FAILURE, INVALID_CREDENTIALS);
    this.errorMap.put(NtStatus.STATUS_ACCESS_DENIED, ACCESS_DENIED);
  }

  public SmbjSmbClient(String host, int port, String shareRoot, LogLevel logLevel) {
    super(host, port, shareRoot, logLevel);
  }

  @Override
  protected SmbFileAttributes<?> doGetFileAttributes(URI uri) throws Exception {
    String pathStr = uri.getPath();
    if (pathStr != null
        && pathStr.replace("smb://" + this.getHost() + ":" + this.getPort(), "").matches(".*(:|\\||>|<|\"|\\?|\\*)+.*")) {
      throw new ConnectionException("The filename, directory name, or volume label syntax is incorrect.");
    }
    if (pathStr.endsWith("/")) {
      pathStr = pathStr.substring(0, pathStr.length() - 1);
    }

    if (pathStr.matches("^[/]{0,1} +$")) {
      throw new ConnectionException("Invalid path: directory path cannot be null nor blank");
    }

    SmbFileAttributes<?> result = null;

    if (this.share.folderExists(pathStr) || this.share.fileExists(pathStr)) {
      result = new SmbjFileAttributes(uri, this.share.getFileInformation(pathStr));
    }

    return result;
  }

  @Override
  public void login(String domain, String username, String password) throws Exception {
    if (share == null) {
      SmbConfig config = SmbConfig.builder()
          .withSecurityProvider(new BCSecurityProvider()).build();
      SMBClient client = new SMBClient(config);
      Connection connection = client.connect(this.getHost());

      AuthenticationContext ac = null;
      if (username == null || username.trim().isEmpty()) {
        ac = AuthenticationContext.anonymous();
      } else {
        ac = new AuthenticationContext(username, password == null ? null : password.toCharArray(), domain);
      }
      Session session = connection.authenticate(ac);

      // Connect to Share
      share = (DiskShare) session.connectShare(this.getShareRoot());
    }
  }

  @Override
  public boolean isConnected() {
    return this.share.isConnected();
  }

  @Override
  public void disconnect() {
    this.close(this.share);
    this.share = null;
  }

  @Override
  public void mkdir(URI uri) {
    this.mkdir(uri.getPath());
  }

  @Override
  public void mkdir(String dirPath) {
    String[] dirs = dirPath.split("/");
    String partialDirPath = null;
    for (int dirIdx = 0; dirIdx < dirs.length; dirIdx++) {
      partialDirPath = (partialDirPath == null ? "" : partialDirPath + "/") + dirs[dirIdx];
      if (!this.share.folderExists(partialDirPath)) {
        this.share.mkdir(partialDirPath);
      }
    }
  }

  @Override
  public List<SmbFileAttributes> list(String directory) {
    List<SmbFileAttributes> result = new ArrayList<>();

    SmbPath path = new SmbPath(this.share.getSmbPath(), directory);
    List<FileIdBothDirectoryInformation> files = this.share.list(path.getPath());

    try {
      if (files != null && !files.isEmpty()) {
        for (FileIdBothDirectoryInformation fileId : files) {
          if (!(fileId.getFileName().equals(".") || fileId.getFileName().equals(".."))) {
            result.add(new SmbjFileAttributes(new URI((directory.startsWith("/") ? "" : "/") + fileId.getFileName()),
                                              share.getFileInformation(directory + "/" + fileId.getFileName())));
          }
        }
      }
    } catch (Exception e) {
      throw exception("Could not list files in " + directory.toString(), e);
    }

    return result;
  }

  @Override
  public void write(String target, InputStream inputStream, FileWriteMode mode) {
    if (inputStream == null) {
      throw exception("Cannot write to file: inputStream is null");
    }

    try (
        File f = this.openFile(target, mode);
        OutputStream os = f.getOutputStream(FileWriteMode.APPEND.equals(mode))) {
      IOUtils.copyLarge(inputStream, os);
      os.flush();
    } catch (Exception e) {
      throw exception("Cannot write to file: " + e.getMessage(), e);
    }
  }

  @Override
  public OutputStream getOutputStream(String target, FileWriteMode mode) {
    if (target == null) {
      throw exception("Cannot write to file: target is null or empty");
    }

    File f = this.openFile(target, mode);
    return f.getOutputStream(FileWriteMode.APPEND.equals(mode));
  }

  private File openFile(String filePath, FileWriteMode mode) {
    Set<FileAttributes> fileAttributes = new HashSet<>();
    fileAttributes.add(FileAttributes.FILE_ATTRIBUTE_NORMAL);
    Set<SMB2CreateOptions> createOptions = new HashSet<>();
    createOptions.add(SMB2CreateOptions.FILE_RANDOM_ACCESS);

    File result = null;
    try {
      if (!this.share.fileExists(filePath)) {
        result = share.openFile(SmbUtils.normalizePath(filePath), EnumSet.of(AccessMask.GENERIC_WRITE), null, SMB2ShareAccess.ALL,
                                SMB2CreateDisposition.FILE_CREATE, EnumSet.of(SMB2CreateOptions.FILE_SEQUENTIAL_ONLY));
      } else {
        result = share.openFile(SmbUtils.normalizePath(filePath), EnumSet.of(AccessMask.GENERIC_WRITE), null, SMB2ShareAccess.ALL,
                                SMB2CreateDisposition.FILE_OPEN, EnumSet.of(SMB2CreateOptions.FILE_SEQUENTIAL_ONLY));
      }

      return result;
    } catch (Exception e) {
      throw exception("Cannot write to file: " + e.getMessage(), e);
    }
  }

  @Override
  public InputStream read(String filePath) {
    if (filePath == null) {
      throw exception("Cannot read from file: filePath is null");
    }

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        File f = share.openFile(filePath, EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL,
                                SMB2CreateDisposition.FILE_OPEN, null);
        InputStream inputStream = f.getInputStream();) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) > 0)
        outputStream.write(buffer, 0, length);
      return new ByteArrayInputStream(outputStream.toByteArray());
    } catch (Exception e) {
      throw exception("Cannot read from file: " + filePath, e, false);
    }
  }

  @Override
  public void doRename(String sourcePath, String newName) {
    if (this.share.folderExists(sourcePath)) {
      try (Directory dir = share.openDirectory(sourcePath, EnumSet.of(AccessMask.DELETE, AccessMask.GENERIC_WRITE), null,
                                               SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null)) {
        dir.rename(newName);
      }
    } else {
      try (File file = share.openFile(sourcePath, EnumSet.of(AccessMask.DELETE, AccessMask.GENERIC_WRITE), null,
                                      SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null)) {
        file.rename(newName);
      }
    }
  }

  @Override
  protected void doCopy(String sourcePath, String targetPath) throws Exception {
    try (File sourceFile = this.openFile(sourcePath, FileWriteMode.OVERWRITE);
        File targetFile = this.openFile(targetPath, FileWriteMode.OVERWRITE)) {
      sourceFile.remoteCopyTo(targetFile);
    }
  }

  @Override
  public void delete(String path) {

    try {
      if (this.share.folderExists(path)) {
        this.share.rmdir(path, true);
      } else {
        this.share.rm(path);
      }
    } catch (Exception e) {
      throw exception("Cannot delete path '" + path + "': " + e.getMessage(), e);
    }

  }

  @Override
  public String getShareRootURL() {
    return null;
  }

  @Override
  public boolean pathIsShareRoot(String path) {
    return path.equals("/");
  }

  @Override
  public boolean isLogLevelEnabled(LogLevel logLevel) {
    return false;
  }

  @Override
  public FileError doGetFileErrorFor(Exception e) {
    FileError result = null;

    SMBApiException sae = null;
    if (e instanceof SMBApiException) {
      sae = (SMBApiException) e;
    } else if (e.getCause() instanceof SMBApiException) {
      sae = (SMBApiException) e.getCause();
    }

    if (sae != null) {
      result = this.errorMap.get(sae.getStatus());
    }

    return result;
  }


  @Override
  protected boolean isApiException(Exception cause) {
    return cause instanceof SMBApiException;
  }

  @Override
  protected boolean exists(String targetPath) throws Exception {
    return this.share.fileExists(targetPath) || this.share.folderExists(targetPath);
  }

  @Override
  public URI resolve(URI uri) {
    try {
      return uri;
    } catch (Exception e) {
      throw new MuleRuntimeException(createStaticMessage("Could not resolve URI: " + e.getMessage()), e);
    }
  }



}
