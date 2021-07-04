/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.provider;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb2.SMBApiException;
import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.internal.connection.SmbClientFactory;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import com.mulesoft.connector.smb.internal.error.exception.SmbConnectionException;
import com.mulesoft.connector.smb.internal.extension.SmbConnector;
import org.mule.extension.file.common.api.FileSystemProvider;
import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.lock.LockFactory;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mule.extension.file.common.api.exceptions.FileError.*;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;

import static java.lang.String.format;

/**
 * An {@link FileSystemProvider} which provides instances of
 * {@link SmbFileSystemConnection} from instances of {@link SmbConnector}
 *
 * @since 1.0
 */
@DisplayName("SMB Connection")
public class SmbConnectionProvider extends FileSystemProvider<SmbFileSystemConnection>
    implements PoolingConnectionProvider<SmbFileSystemConnection> {


  private static final Logger LOGGER = LoggerFactory.getLogger(SmbConnectionProvider.class);

  private static final String TIMEOUT_CONFIGURATION = "Timeout Configuration";
  private static final String SMB_ERROR_MESSAGE_MASK =
      "Could not establish SMB connection (host: '\\\\%s\\%s', user: '%s\\%s'): %s";
  private static final String SSH_DISCONNECTION_MESSAGE = "SSH_MSG_DISCONNECT";
  private static final String TIMEOUT = "timeout";

  private static AtomicBoolean alreadyLoggedConnectionTimeoutWarning = new AtomicBoolean(false);
  private static AtomicBoolean alreadyLoggedResponseTimeoutWarning = new AtomicBoolean(false);

  private HashMap<NtStatus, FileError> errorMap = new HashMap<>();

  {
    this.errorMap.put(NtStatus.STATUS_CONNECTION_DISCONNECTED, DISCONNECTED);
    this.errorMap.put(NtStatus.STATUS_LOGON_FAILURE, INVALID_CREDENTIALS);
    this.errorMap.put(NtStatus.STATUS_ACCESS_DENIED, ACCESS_DENIED);
    this.errorMap.put(NtStatus.STATUS_OBJECT_NAME_NOT_FOUND, FILE_DOESNT_EXIST);
  }

  @Inject
  private LockFactory lockFactory;

  /**
   * The directory to be considered as the root of every relative path used with this connector. If not provided, it will default
   * to the remote server default.
   */
  @Parameter
  @Optional
  @Summary("The directory to be considered as the root of every relative path used with this connector")
  @DisplayName("Working Directory")
  private String workingDir = null;

  @ParameterGroup(name = TIMEOUT_CONFIGURATION)
  private TimeoutSettings timeoutSettings = new TimeoutSettings();

  @ParameterGroup(name = CONNECTION)
  private SmbConnectionSettings connectionSettings = new SmbConnectionSettings();

  /**
   * The log level
   */
  @Parameter
  @Optional(defaultValue = "WARN")
  @Summary("Log level. Used by Logger operation to determine if messages " +
      "whether log messages should be written or not")
  @Placement(order = 3)
  private LogLevel logLevel;


  private SmbClientFactory clientFactory = new SmbClientFactory();

  @Override
  public SmbFileSystemConnection connect() throws ConnectionException {
    checkConnectionTimeoutPrecision();
    checkSocketTimeoutPrecision();
    checkReadTimeoutPrecision();
    checkWriteTimeoutPrecision();
    checkTransactionTimeoutPrecision();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(format("Connecting to host: '%s' at port: '%d'", connectionSettings.getHost(), connectionSettings.getPort()));
    }
    SmbClient client = clientFactory.createInstance(connectionSettings.getHost(), connectionSettings.getPort(),
                                                    connectionSettings.getShareRoot(), connectionSettings.getDfsEnabled(),
                                                    this.logLevel);
    client.setPassword(connectionSettings.getPassword());

    try {
      client.login(connectionSettings.getDomain(), connectionSettings.getUsername());
    } catch (Exception e) {
      handleException(e);
    }

    return new SmbFileSystemConnection(client, lockFactory);
  }

  @Override
  public void disconnect(SmbFileSystemConnection smbFileSystem) {
    smbFileSystem.disconnect();
  }

  @Override
  public ConnectionValidationResult validate(SmbFileSystemConnection smbFileSystem) {
    return smbFileSystem.validateConnection();
  }

  void setPort(int port) {
    connectionSettings.setPort(port);
  }

  void setHost(String host) {
    connectionSettings.setHost(host);
  }

  void setUsername(String username) {
    connectionSettings.setUsername(username);
  }

  void setPassword(String password) {
    connectionSettings.setPassword(password);
  }

  void setShareRoot(String shareRoot) {
    this.connectionSettings.setShareRoot(shareRoot);
  }

  void setDomain(String domain) {
    this.connectionSettings.setDomain(domain);
  }

  void setClientFactory(SmbClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

  @Override
  public String getWorkingDir() {
    //Working Dir is not used for SMB connector
    throw new RuntimeException("workingDir property should not be used");
  }

  protected Integer getConnectionTimeout() {
    return timeoutSettings.getConnectionTimeout();
  }

  protected TimeUnit getConnectionTimeoutUnit() {
    return timeoutSettings.getConnectionTimeoutUnit();
  }

  protected Integer getSocketTimeout() {
    return timeoutSettings.getSocketTimeout();
  }

  protected TimeUnit getSocketTimeoutUnit() {
    return timeoutSettings.getSocketTimeoutUnit();
  }

  protected Integer getReadTimeout() {
    return timeoutSettings.getReadTimeout();
  }

  protected TimeUnit getReadTimeoutUnit() {
    return timeoutSettings.getReadTimeoutUnit();
  }

  protected Integer getWriteTimeout() {
    return timeoutSettings.getWriteTimeout();
  }

  protected TimeUnit getWriteTimeoutUnit() {
    return timeoutSettings.getWriteTimeoutUnit();
  }

  protected Integer getTransactionTimeout() {
    return timeoutSettings.getTransactionTimeout();
  }

  protected TimeUnit getTransactionTimeoutUnit() {
    return timeoutSettings.getTransactionTimeoutUnit();
  }


  public void setConnectionTimeout(Integer connectionTimeout) {
    timeoutSettings.setConnectionTimeout(connectionTimeout);
  }

  public void setConnectionTimeoutUnit(TimeUnit connectionTimeoutUnit) {
    timeoutSettings.setConnectionTimeoutUnit(connectionTimeoutUnit);
  }

  public void setSocketTimeout(Integer socketTimeout) {
    timeoutSettings.setSocketTimeout(socketTimeout);
  }

  public void setSocketTimeoutUnit(TimeUnit socketTimeoutUnit) {
    timeoutSettings.setSocketTimeoutUnit(socketTimeoutUnit);
  }

  public void setReadTimeout(Integer readTimeout) {
    timeoutSettings.setReadTimeout(readTimeout);
  }

  public void setReadTimeoutUnit(TimeUnit readTimeoutUnit) {
    timeoutSettings.setReadTimeoutUnit(readTimeoutUnit);
  }

  public void setWriteTimeout(Integer writeTimeout) {
    timeoutSettings.setWriteTimeout(writeTimeout);
  }

  public void setWriteTimeoutUnit(TimeUnit writeTimeoutUnit) {
    timeoutSettings.setWriteTimeoutUnit(writeTimeoutUnit);
  }

  public void setTransactionTimeout(Integer transactionTimeout) {
    timeoutSettings.setTransactionTimeout(transactionTimeout);
  }

  public void setTransactionTimeoutUnit(TimeUnit transactionTimeoutUnit) {
    timeoutSettings.setTransactionTimeoutUnit(transactionTimeoutUnit);
  }


  /**
   * Handles a {@link SMBApiException}, introspects their cause or message to return a {@link ConnectionException} indicating with a
   * {@link FileError} the kind of failure.
   *
   * @param e The exception to handle
   * @throws ConnectionException Indicating the kind of failure
   */
  private void handleException(Exception e) throws ConnectionException {
    FileError error = getFileErrorFor(e);
    if (error != null) {
      throw new SmbConnectionException(getErrorMessage(connectionSettings, e.getMessage()), e, error);
    }
    throw new ConnectionException(getErrorMessage(connectionSettings, e.getMessage()), e);
  }

  private FileError getFileErrorFor(Exception e) {
    FileError result = this.doGetFileErrorFor(e);
    if (result == null) {
      if (e instanceof SocketTimeoutException || e.getMessage().contains("connect timed out")) {
        result = CONNECTION_TIMEOUT;
      } else if (e instanceof UnknownHostException || e.getCause() instanceof UnknownHostException) {
        result = UNKNOWN_HOST;
      } else if (e instanceof org.mule.runtime.core.api.connector.ConnectException
          || e.getCause() instanceof org.mule.runtime.core.api.connector.ConnectException) {
        result = CANNOT_REACH;
      }
    }
    return result;
  }

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


  private String getErrorMessage(SmbConnectionSettings connectionSettings, String message) {
    return format(SMB_ERROR_MESSAGE_MASK, connectionSettings.getHost(), connectionSettings.getShareRoot(),
                  connectionSettings.getDomain(), connectionSettings.getUsername(), message);
  }

  private void checkConnectionTimeoutPrecision() {
    if (!supportedTimeoutPrecision(getConnectionTimeoutUnit(), getConnectionTimeout())
        && alreadyLoggedConnectionTimeoutWarning.compareAndSet(false, true)) {
      LOGGER.warn("Connection timeout configuration not supported. Minimum value allowed is 1 millisecond.");
    }
  }

  private void checkSocketTimeoutPrecision() {
    if (!supportedTimeoutPrecision(getSocketTimeoutUnit(), getSocketTimeout())
        && alreadyLoggedResponseTimeoutWarning.compareAndSet(false, true)) {
      LOGGER.warn("Read timeout configuration not supported. Minimum value allowed is 1 millisecond.");
    }
  }

  private void checkReadTimeoutPrecision() {
    if (!supportedTimeoutPrecision(getReadTimeoutUnit(), getReadTimeout())
        && alreadyLoggedResponseTimeoutWarning.compareAndSet(false, true)) {
      LOGGER.warn("Write timeout configuration not supported. Minimum value allowed is 1 millisecond.");
    }
  }

  private void checkWriteTimeoutPrecision() {
    if (!supportedTimeoutPrecision(getWriteTimeoutUnit(), getWriteTimeout())
        && alreadyLoggedResponseTimeoutWarning.compareAndSet(false, true)) {
      LOGGER.warn("Transaction timeout configuration not supported. Minimum value allowed is 1 millisecond.");
    }
  }

  private void checkTransactionTimeoutPrecision() {
    if (!supportedTimeoutPrecision(getTransactionTimeoutUnit(), getTransactionTimeout())
        && alreadyLoggedResponseTimeoutWarning.compareAndSet(false, true)) {
      LOGGER.warn("Response timeout configuration not supported. Minimum value allowed is 1 millisecond.");
    }
  }



  private boolean supportedTimeoutPrecision(TimeUnit timeUnit, Integer timeout) {
    return timeUnit != null && timeout != null && (timeUnit.toMillis(timeout) >= 1 || timeout == 0);
  }

}
