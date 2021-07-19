/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.provider;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb2.SMBApiException;
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
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.EnumMap;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.mule.extension.file.common.api.exceptions.FileError.*;
import static org.mule.runtime.extension.api.annotation.param.ParameterGroup.CONNECTION;

/**
 * An {@link FileSystemProvider} which provides instances of
 * {@link SmbFileSystemConnection} from instances of {@link SmbConnector}
 *
 * @since 1.0
 */
@DisplayName("SMB Connection")
public class SmbConnectionProvider extends FileSystemProvider<SmbFileSystemConnection>
    implements PoolingConnectionProvider<SmbFileSystemConnection> {


  private static final Logger logger = LoggerFactory.getLogger(SmbConnectionProvider.class);

  private static final String TIMEOUT_CONFIGURATION = "Timeout Configuration";
  private static final String SMB_ERROR_MESSAGE_MASK =
      "Could not establish SMB connection (host: '\\\\%s\\%s', user: '%s\\%s'): %s";

  private static EnumMap<NtStatus, FileError> errorMap = new EnumMap<>(NtStatus.class);

  static {
    errorMap.put(NtStatus.STATUS_CONNECTION_DISCONNECTED, DISCONNECTED);
    errorMap.put(NtStatus.STATUS_LOGON_FAILURE, INVALID_CREDENTIALS);
    errorMap.put(NtStatus.STATUS_ACCESS_DENIED, ACCESS_DENIED);
    errorMap.put(NtStatus.STATUS_OBJECT_NAME_NOT_FOUND, FILE_DOESNT_EXIST);
  }

  @Inject
  private LockFactory lockFactory;

  @ParameterGroup(name = TIMEOUT_CONFIGURATION)
  private final TimeoutSettings timeoutSettings = new TimeoutSettings();

  @ParameterGroup(name = CONNECTION)
  private final SmbConnectionSettings connectionSettings = new SmbConnectionSettings();

  private SmbClientFactory clientFactory = new SmbClientFactory();

  @Override
  public SmbFileSystemConnection connect() throws ConnectionException {
    if (logger.isDebugEnabled()) {
      logger.debug(format("Connecting to host: '%s' at port: '%d'", connectionSettings.getHost(), connectionSettings.getPort()));
    }
    SmbClient client = clientFactory.createInstance(connectionSettings.getHost(), connectionSettings.getPort(),
                                                    connectionSettings.getShareRoot(), connectionSettings.isDfsEnabled());
    client.setPassword(connectionSettings.getPassword());
    client.setSocketTimeout(this.getSocketTimeoutUnit(), this.getSocketTimeout());
    client.setReadTimeout(this.getReadTimeoutUnit(), this.getReadTimeout());
    client.setWriteTimeout(this.getWriteTimeoutUnit(), this.getWriteTimeout());
    client.setTransactionTimeout(this.getTransactionTimeoutUnit(), this.getTransactionTimeout());
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

  @Override
  public String getWorkingDir() {
    throw new UnsupportedOperationException("workingDir property not implemented");
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

  /**
   * Handles a {@link SMBApiException}, introspects their cause or message to return a {@link ConnectionException} indicating with a
   * {@link FileError} the kind of failure.
   *
   * @param e The exception to handle
   * @throws ConnectionException Indicating the kind of failure
   */
  private void handleException(Throwable e) throws ConnectionException {
    FileError error = null;

    if (e instanceof SMBApiException) {
      error = getFileErrorFor((SMBApiException) e);
    } else {
      if (e instanceof SocketTimeoutException) {
        error = CONNECTION_TIMEOUT;
      } else if (e instanceof UnknownHostException) {
        error = UNKNOWN_HOST;
      } else if (e instanceof ConnectException) {
        error = CANNOT_REACH;
      }
    }

    if (error != null) {
      throw new SmbConnectionException(getErrorMessage(connectionSettings, e.getMessage()), e, error);
    }
    throw new ConnectionException(getErrorMessage(connectionSettings, e.getMessage()), e);
  }

  private FileError getFileErrorFor(SMBApiException sae) {
    return errorMap.get(sae.getStatus());
  }

  private String getErrorMessage(SmbConnectionSettings connectionSettings, String message) {
    return format(SMB_ERROR_MESSAGE_MASK, connectionSettings.getHost(), connectionSettings.getShareRoot(),
                  connectionSettings.getDomain(), connectionSettings.getUsername(), message);
  }

  protected void setClientFactory(SmbClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

}
