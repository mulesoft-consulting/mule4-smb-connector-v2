/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.client;

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
import com.mulesoft.connector.smb.internal.connection.FileCopyMode;
import com.mulesoft.connector.smb.internal.error.exception.SmbConnectionException;
import com.mulesoft.connector.smb.internal.utils.SmbUtils;
import org.mule.extension.file.common.api.FileWriteMode;
import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.extension.file.common.api.exceptions.IllegalPathException;
import org.mule.extension.file.common.api.util.UriUtils;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.util.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.mulesoft.connector.smb.internal.utils.SmbUtils.normalizePath;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.mule.extension.file.common.api.exceptions.FileError.*;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.util.collection.Collectors.toImmutableList;

public class SmbClient {

  private String host;
  private int port;
  private String shareRoot;
  private String password;
  private LogLevel logLevel;
  private boolean dfsEnabled;

  private TimeUnit connectionTimeoutUnit = TimeUnit.SECONDS;
  private Integer connectionTimeout = Integer.valueOf(10);
  private TimeUnit socketTimeoutUnit = TimeUnit.SECONDS;
  private Integer socketTimeout = Integer.valueOf(10);
  private TimeUnit readTimeoutUnit = TimeUnit.SECONDS;
  private Integer readTimeout = Integer.valueOf(60);
  private TimeUnit writeTimeoutUnit = TimeUnit.SECONDS;
  private Integer writeTimeout = Integer.valueOf(60);
  private TimeUnit transactionTimeoutUnit = TimeUnit.SECONDS;
  private Integer transactionTimeout = Integer.valueOf(60);

  private DiskShare share;

  public SmbClient(String host, int port, String shareRoot, boolean dfsEnabled, LogLevel logLevel) {
    this.host = host;
    this.port = port;
    this.shareRoot = shareRoot;
    this.dfsEnabled = dfsEnabled;
    this.logLevel = logLevel;
  }

  public SmbFileAttributes getAttributes(URI uri) throws Exception {
    String pathStr = uri.getPath();
    if (pathStr != null
        && pathStr.replace("smb://" + this.getHost() + ":" + this.getPort(), "").matches(".*(:|\\||>|<|\"|\\?|\\*)+.*")) {
      throw new ConnectionException("The filename, directory name, or volume label syntax is incorrect.");
    }

    if (pathStr.endsWith("/.")) {
      pathStr = pathStr.substring(0, pathStr.length() - 1);
    }

    if (pathStr.endsWith("/")) {
      pathStr = pathStr.substring(0, pathStr.length() - 1);
    }

    if (pathStr.matches("^[/]{0,1} +$")) {
      throw new ConnectionException("Invalid path: directory path cannot be null nor blank");
    }

    SmbFileAttributes result = null;

    if (this.share.folderExists(pathStr) || this.share.fileExists(pathStr)) {
      result = new SmbFileAttributes(uri, this.share.getFileInformation(pathStr));
    }

    return result;
  }

  public void login(String domain, String username) throws IOException {
    if (share == null || !share.isConnected()) {
      SmbConfig config = SmbConfig.builder()
          .withSecurityProvider(new BCSecurityProvider())
          .withReadTimeout(this.readTimeout, this.readTimeoutUnit)
          .withWriteTimeout(this.writeTimeout, this.writeTimeoutUnit)
          .withSoTimeout(this.socketTimeout, this.socketTimeoutUnit)
          .withTransactTimeout(this.transactionTimeout,
                               this.transactionTimeoutUnit)
          .withDfsEnabled(this.dfsEnabled)
          .build();
      SMBClient client = new SMBClient(config);
      Connection connection = client.connect(this.getHost(), this.port);
      AuthenticationContext ac = null;
      if (username == null || username.trim().isEmpty()) {
        ac = AuthenticationContext.anonymous();
      } else {
        ac = new AuthenticationContext(username, password == null ? null : password.toCharArray(), domain);
      }
      Session session = connection.authenticate(ac);
      share = (DiskShare) session.connectShare(this.getShareRoot());
    }
  }

  public boolean isConnected() {
    return this.share.isConnected();
  }

  public void disconnect() {
    this.close(this.share);
  }

  public void mkdir(URI uri) {
    this.mkdir(uri.getPath());
  }

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

  public List<SmbFileAttributes> list(String directory) {
    SmbPath path = new SmbPath(this.share.getSmbPath(), directory);
    List<FileIdBothDirectoryInformation> files;

    try {
      files = this.share.list(path.getPath());
    } catch (SMBApiException e) {
      throw exception("Found exception trying to list path " + path, e);
    }

    if (isEmpty(files)) {
      return emptyList();
    }

    return files.stream().filter(entry -> !".".equals(entry.getFileName()) && !"..".equals(entry.getFileName()))
        .map(entry -> createSmbFileAttribute(directory, entry))
        .collect(toImmutableList());
  }

  private SmbFileAttributes createSmbFileAttribute(String directory, FileIdBothDirectoryInformation entry) {
    try {
      return new SmbFileAttributes(
                                   new URI(
                                           (directory.startsWith("/") ? "" : "/") + entry.getFileName()),
                                   share.getFileInformation(directory + "/" + entry.getFileName()));
    } catch (URISyntaxException e) {
      throw new IllegalPathException("Cannot convert given path into a valid Uri", e);
    } catch (Exception e) {
      throw exception("Found exception trying to list path " + directory, e);
    }
  }

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

  public void rename(String sourcePath, String newName, boolean overwrite) {

    if (sourcePath == null) {
      throw exception("Cannot rename sourcePath: sourcePath is null.");
    }

    if (newName == null) {
      throw exception("Cannot rename sourcePath: newName is null.");
    }

    try {
      if (exists(newName)) {
        if (overwrite && exists(sourcePath)) {
          delete(newName);
        }
      }
      doRename(sourcePath, newName);
    } catch (Exception e) {
      throw exception("Cannot rename sourcePath: " + e.getMessage(), e);
    }
  }

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

  public void copy(String sourcePath, String targetPath) {
    copyOrMove(sourcePath, targetPath, FileCopyMode.COPY);
  }

  public void move(String sourcePath, String targetPath) {
    copyOrMove(sourcePath, targetPath, FileCopyMode.MOVE);
  }

  private void copyOrMove(String sourcePath, String targetPath, FileCopyMode mode) {
    try {
      if (sourcePath == null) {
        throw new MuleRuntimeException(createStaticMessage("Cannot " + mode.label()
            + " sourcePath to targetDir: sourcePath is null."));
      }

      if (targetPath == null) {
        throw new MuleRuntimeException(createStaticMessage("Cannot " + mode.label()
            + " sourcePath to targetDir: targetDir is null."));
      }

      doCopy(sourcePath, targetPath);

      if (this.exists(targetPath) && FileCopyMode.MOVE.equals(mode)) {
        this.delete(sourcePath);
      }
    } catch (Exception e) {
      throw exception("Could not " + mode.label() + "sourcePath to targetDir: " + e.getMessage(), e);
    }
  }

  private void doCopy(String sourcePath, String targetPath) throws Exception {
    try (File sourceFile = this.openFile(sourcePath, FileWriteMode.OVERWRITE);
        File targetFile = this.openFile(targetPath, FileWriteMode.OVERWRITE)) {
      sourceFile.remoteCopyTo(targetFile);
    }
  }

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

  public String getShareRootURL() {
    return null;
  }

  public boolean pathIsShareRoot(String path) {
    return path.equals("/");
  }

  public boolean isLogLevelEnabled(LogLevel logLevel) {
    return false;
  }

  protected boolean isApiException(Exception cause) {
    return cause instanceof SMBApiException;
  }

  public boolean exists(String targetPath) throws Exception {
    return this.share.fileExists(targetPath) || this.share.folderExists(targetPath);
  }

  public URI resolve(URI uri) {
    try {
      return uri;
    } catch (Exception e) {
      throw new MuleRuntimeException(createStaticMessage("Could not resolve URI: " + e.getMessage()), e);
    }
  }


  public void setPassword(String password) {
    this.password = password;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getShareRoot() {
    return shareRoot;
  }

  public LogLevel getLogLevel() {
    return logLevel;
  }

  private void close(AutoCloseable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        //Does nothing
      }
    }
  }

  public URI resolvePath(String filePath) {
    URI result = null;

    if (filePath != null) {
      String actualFilePath = filePath;
      if (!actualFilePath.startsWith("/") && !actualFilePath.startsWith("smb://")) {
        actualFilePath = "/" + actualFilePath;
      }
      result = resolve(UriUtils.createUri(normalizePath(actualFilePath)));
    }
    return result;
  }


  protected RuntimeException exception(String message) {
    return this.exception(message, null);
  }

  protected RuntimeException exception(String message, Exception cause) {
    return this.exception(message, cause, true);
  }

  protected RuntimeException exception(String message, Exception cause, boolean convertApiExceptionToConnectionException) {

    if (cause == null) {
      return new MuleRuntimeException(createStaticMessage(message));
    }

    if (convertApiExceptionToConnectionException && isApiException(cause)) {
      return new MuleRuntimeException(createStaticMessage(message),
                                      new SmbConnectionException(message, cause, FileError.CONNECTIVITY));
    }

    return new MuleRuntimeException(createStaticMessage(message), cause);
  }

}
