/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.client;

import com.hierynomus.msdtyp.AccessMask;
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
import com.mulesoft.connector.smb.api.SmbFileAttributes;
import com.mulesoft.connector.smb.internal.error.exception.SmbConnectionException;
import com.mulesoft.connector.smb.internal.utils.SmbUtils;
import org.mule.extension.file.common.api.FileWriteMode;
import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.extension.file.common.api.util.UriUtils;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.core.api.util.StringUtils;

import java.io.*;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.util.collection.Collectors.toImmutableList;

public class SmbClient {

  private final String host;
  private final int port;
  private final String shareRoot;
  private String password;
  private final boolean dfsEnabled;

  private TimeUnit socketTimeoutUnit = TimeUnit.SECONDS;
  private Integer socketTimeout = 10;
  private TimeUnit readTimeoutUnit = TimeUnit.SECONDS;
  private Integer readTimeout = 60;
  private TimeUnit writeTimeoutUnit = TimeUnit.SECONDS;
  private Integer writeTimeout = 60;
  private TimeUnit transactionTimeoutUnit = TimeUnit.SECONDS;
  private Integer transactionTimeout = 60;

  private SMBClient client;
  private DiskShare share;

  public SmbClient(String host, int port, String shareRoot, boolean dfsEnabled) {
    this.host = host;
    this.port = port;
    this.shareRoot = shareRoot;
    this.dfsEnabled = dfsEnabled;
  }

  public SmbFileAttributes getAttributes(URI uri) throws ConnectionException {
    String pathStr = uri.getPath();
    Pattern p = Pattern.compile("[:\\|><\"\\?\\*]", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(pathStr);
    if (m.find()) {
      throw new ConnectionException("The filename, directory name, or volume label syntax is incorrect.");
    }

    if (pathStr.endsWith("/.")) {
      pathStr = pathStr.substring(0, pathStr.length() - 1);
    }

    if (pathStr.endsWith("/")) {
      pathStr = pathStr.substring(0, pathStr.length() - 1);
    }

    if (pathStr.matches("^[/]? +$")) {
      throw new ConnectionException("Invalid path: directory path cannot be null nor blank");
    }

    SmbFileAttributes result = null;

    if (this.share.folderExists(pathStr) || this.share.fileExists(pathStr)) {
      result = new SmbFileAttributes(uri, this.share.getFileInformation(pathStr));
    }

    return result;
  }

  public void login(String domain, String username) throws IOException {
    if (this.shareRoot == null) {
      throw new IllegalArgumentException("shareRoot is null");
    }
    client = new SMBClient(getConfig());
    Connection connection = client.connect(this.getHost(), this.port);
    Session session = connection.authenticate(getAuthenticationContext(domain, username));
    share = (DiskShare) session.connectShare(this.getShareRoot());
  }

  private AuthenticationContext getAuthenticationContext(String domain, String username) {
    AuthenticationContext result = AuthenticationContext.anonymous();
    if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
      result = new AuthenticationContext(username, password.toCharArray(), domain);
    }
    return result;
  }

  private SmbConfig getConfig() {
    SmbConfig.Builder configBuilder = SmbConfig.builder()
        .withSecurityProvider(new BCSecurityProvider())
        .withDfsEnabled(this.dfsEnabled);

    if (this.socketTimeoutUnit != null && this.socketTimeout != null) {
      configBuilder.withSoTimeout(this.socketTimeout, this.socketTimeoutUnit);
    }

    if (this.readTimeoutUnit != null && this.readTimeout != null) {
      configBuilder.withReadTimeout(this.readTimeout, this.readTimeoutUnit);
    }

    if (this.writeTimeoutUnit != null && this.writeTimeout != null) {
      configBuilder.withWriteTimeout(this.writeTimeout, this.writeTimeoutUnit);
    }

    if (this.transactionTimeoutUnit != null && this.transactionTimeout != null) {
      configBuilder.withTransactTimeout(this.transactionTimeout,
                                        this.transactionTimeoutUnit);
    }

    return configBuilder.build();
  }

  public boolean isConnected() {
    return this.share.isConnected();
  }

  public void disconnect() {
    this.close(this.share);
    this.close(this.client);
  }

  public void mkdir(URI uri) {
    this.mkdir(uri.getPath());
  }

  public void mkdir(String dirPath) {
    String[] dirs = dirPath.split("/");
    String partialDirPath = "";
    for (String dir : dirs) {
      partialDirPath = UriUtils.createUri(partialDirPath, dir).getPath();
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

    return files.stream().filter(entry -> !".".equals(entry.getFileName()) && !"..".equals(entry.getFileName()))
        .map(entry -> createSmbFileAttribute(directory, entry))
        .collect(toImmutableList());
  }

  private SmbFileAttributes createSmbFileAttribute(String directory, FileIdBothDirectoryInformation entry) {
    try {
      URI path = UriUtils.createUri(directory, entry.getFileName());
      return new SmbFileAttributes(path,
                                   share.getFileInformation(path.getPath()));
    } catch (Exception e) {
      throw exception("Found exception trying to list path " + directory, e);
    }
  }

  public void write(String target, InputStream inputStream, FileWriteMode mode) {
    try (
        File f = this.openFile(target);
        OutputStream os = f.getOutputStream(FileWriteMode.APPEND.equals(mode))) {
      IOUtils.copyLarge(inputStream, os);
      os.flush();
    } catch (Exception e) {
      throw exception("Cannot write to file: " + e.getMessage(), e);
    }
  }

  public OutputStream getOutputStream(String target, FileWriteMode mode) {
    return this.openFile(target).getOutputStream(FileWriteMode.APPEND.equals(mode));
  }

  private File openFile(String filePath) {
    File result;
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
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        File f = share.openFile(filePath, EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL,
                                SMB2CreateDisposition.FILE_OPEN, null);
        InputStream inputStream = f.getInputStream()) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) > 0)
        outputStream.write(buffer, 0, length);
      return new ByteArrayInputStream(outputStream.toByteArray());
    } catch (Exception e) {
      throw exception(format("Cannot read from file '%s': %s", filePath, e.getMessage()), e, false);
    }
  }

  public void rename(String sourcePath, String newName) {
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

  public boolean pathIsShareRoot(String path) {
    return path.equals("/");
  }

  protected boolean isApiException(Exception cause) {
    return cause instanceof SMBApiException;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getHost() {
    return host;
  }

  public String getShareRoot() {
    return shareRoot;
  }

  private void close(AutoCloseable closeable) {
    try {
      closeable.close();
    } catch (Exception e) {
      //Does nothing
    }
  }

  protected RuntimeException exception(String message, Exception cause) {
    return this.exception(message, cause, true);
  }

  protected RuntimeException exception(String message, Exception cause, boolean convertApiExceptionToConnectionException) {

    if (convertApiExceptionToConnectionException && isApiException(cause)) {
      return new MuleRuntimeException(createStaticMessage(message),
                                      new SmbConnectionException(message, cause, FileError.CONNECTIVITY));
    }

    return new MuleRuntimeException(createStaticMessage(message), cause);

  }

  public void setSocketTimeout(TimeUnit socketTimeoutUnit, Integer socketTimeout) {
    this.socketTimeoutUnit = socketTimeoutUnit;
    this.socketTimeout = socketTimeout;
  }

  public void setReadTimeout(TimeUnit readTimeoutUnit, Integer readTimeout) {
    this.readTimeoutUnit = readTimeoutUnit;
    this.readTimeout = readTimeout;
  }

  public void setWriteTimeout(TimeUnit writeTimeoutUnit, Integer writeTimeout) {
    this.writeTimeoutUnit = writeTimeoutUnit;
    this.writeTimeout = writeTimeout;
  }

  public void setTransactionTimeout(TimeUnit transactionTimeoutUnit, Integer transactionTimeout) {
    this.transactionTimeoutUnit = transactionTimeoutUnit;
    this.transactionTimeout = transactionTimeout;
  }

}
