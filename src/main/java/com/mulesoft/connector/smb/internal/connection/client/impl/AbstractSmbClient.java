/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.client.impl;

import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.api.SmbFileAttributes;
import com.mulesoft.connector.smb.internal.connection.FileCopyMode;
import com.mulesoft.connector.smb.internal.connection.SmbClient;
import com.mulesoft.connector.smb.internal.error.exception.SmbConnectionException;
import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.extension.file.common.api.util.UriUtils;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.connector.ConnectException;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import static com.mulesoft.connector.smb.internal.utils.SmbUtils.normalizePath;
import static org.mule.extension.file.common.api.exceptions.FileError.*;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

public abstract class AbstractSmbClient implements SmbClient {

  private String host;
  private int port;
  private String shareRoot;
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

  public AbstractSmbClient(String host, int port, String shareRoot, LogLevel logLevel, boolean dfsEnabled) {
    this.host = host;
    this.port = port;
    this.shareRoot = shareRoot;
    this.logLevel = logLevel;
    this.dfsEnabled = dfsEnabled;
  }

  @Override
  public SmbFileAttributes<?> getAttributes(URI uri) throws Exception {
    return this.doGetFileAttributes(uri);
  }

  protected abstract SmbFileAttributes<?> doGetFileAttributes(URI uri) throws Exception;

  protected String getHost() {
    return host;
  }

  protected int getPort() {
    return port;
  }

  public String getShareRoot() {
    return shareRoot;
  }

  public LogLevel getLogLevel() {
    return logLevel;
  }

  protected void close(AutoCloseable closeable) {
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

  protected abstract void doRename(String sourcePath, String newName) throws Exception;

  protected abstract boolean exists(String targetPath) throws Exception;

  protected abstract void doCopy(String sourcePath, String targetPath) throws Exception;

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

  protected abstract boolean isApiException(Exception cause);

  @Override
  public FileError getFileErrorFor(Exception e) {
    FileError result = this.doGetFileErrorFor(e);
    if (result == null) {
      if (e instanceof SocketTimeoutException || e.getMessage().contains("connect timed out")) {
        result = CONNECTION_TIMEOUT;
      } else if (e instanceof UnknownHostException || e.getCause() instanceof UnknownHostException) {
        result = UNKNOWN_HOST;
      } else if (e instanceof ConnectException || e.getCause() instanceof ConnectException) {
        result = CANNOT_REACH;
      }
    }
    return result;
  }

  protected abstract FileError doGetFileErrorFor(Exception e);

  protected boolean getDfsEnabled() {
    return this.dfsEnabled;
  }

  protected TimeUnit getConnectionTimeoutUnit() {
    return connectionTimeoutUnit;
  }

  protected Integer getConnectionTimeout() {
    return connectionTimeout;
  }

  protected TimeUnit getSocketTimeoutUnit() {
    return socketTimeoutUnit;
  }

  protected Integer getSocketTimeout() {
    return socketTimeout;
  }

  protected TimeUnit getReadTimeoutUnit() {
    return readTimeoutUnit;
  }

  protected Integer getReadTimeout() {
    return readTimeout;
  }

  protected TimeUnit getWriteTimeoutUnit() {
    return writeTimeoutUnit;
  }

  protected Integer getWriteTimeout() {
    return writeTimeout;
  }

  protected TimeUnit getTransactionTimeoutUnit() {
    return transactionTimeoutUnit;
  }

  protected Integer getTransactionTimeout() {
    return transactionTimeout;
  }

  @Override
  public void setConnectionTimeout(TimeUnit unit, Integer timeout) {
    this.connectionTimeoutUnit = unit;
    this.connectionTimeout = timeout;
  }

  @Override
  public void setSocketTimeout(TimeUnit unit, Integer timeout) {
    this.socketTimeoutUnit = unit;
    this.socketTimeout = timeout;
  }

  @Override
  public void setReadTimeout(TimeUnit unit, Integer timeout) {
    this.readTimeoutUnit = unit;
    this.readTimeout = timeout;
  }

  @Override
  public void setWriteTimeout(TimeUnit unit, Integer timeout) {
    this.writeTimeoutUnit = unit;
    this.writeTimeout = timeout;
  }

  @Override
  public void setTransactionTimeout(TimeUnit unit, Integer timeout) {
    this.transactionTimeoutUnit = unit;
    this.transactionTimeout = timeout;
  }

}
