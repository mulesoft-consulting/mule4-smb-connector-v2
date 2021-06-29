/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.provider;

import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.internal.connection.SmbClient;
import com.mulesoft.connector.smb.internal.connection.SmbClientFactory;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import com.mulesoft.connector.smb.internal.error.exception.SmbConnectionException;
import com.mulesoft.connector.smb.internal.extension.SmbConnector;
import org.mule.extension.file.common.api.FileSystemProvider;
import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.exception.MuleRuntimeException;
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

  private static final String TIMEOUT_CONFIGURATION = "Timeout Configuration";
  private static final Logger LOGGER = LoggerFactory.getLogger(SmbConnectionProvider.class);
  private static final String SMB_ERROR_MESSAGE_MASK =
      "Could not establish SMB connection (host: '%s', domain: %s, user: %s, share root: '%s', logLevel: '%s'): %s";

  @Inject
  private LockFactory lockFactory;

  @ParameterGroup(name = CONNECTION)
  private SmbConnectionSettings connectionSettings = new SmbConnectionSettings();

  @ParameterGroup(name = TIMEOUT_CONFIGURATION)
  private TimeoutSettings timeoutSettings = new TimeoutSettings();

  /**
   * The log level
   */
  @Parameter
  @Optional(defaultValue = "WARN")
  @Summary("Log level. Used by Logger operation to determine if messages " +
      "whether log messages should be written or not")
  @Placement(order = 7)
  private LogLevel logLevel;

  private SmbClientFactory clientFactory = new SmbClientFactory();

  @Override
  public SmbFileSystemConnection connect() throws ConnectionException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(format("Connecting to SMB server (host: '%s', domain: '%s', user: '%s', share Root: '%s')",
                          connectionSettings.getHost(),
                          connectionSettings.getDomain(), connectionSettings.getUsername(), connectionSettings.getShareRoot()));
    }
    SmbClient client = clientFactory.createInstance(connectionSettings.getHost(), connectionSettings.getPort(),
                                                    connectionSettings.getShareRoot(), logLevel,
                                                    connectionSettings.getDfsEnabled(), timeoutSettings);
    try {
      client.login(connectionSettings.getDomain(), connectionSettings.getUsername(), connectionSettings.getPassword());
    } catch (Exception e) {
      FileError error = null;
      if (client != null) {
        error = client.getFileErrorFor(e);
      }
      if (error != null) {
        throw new SmbConnectionException(getErrorMessage(e.getMessage()), e, error);
      } else {
        throw new ConnectionException(getErrorMessage(e.getMessage()), e);
      }
    }

    return new SmbFileSystemConnection(client, lockFactory);
  }

  @Override
  public void disconnect(SmbFileSystemConnection fileSystem) {
    fileSystem.disconnect();
  }

  @Override
  public ConnectionValidationResult validate(SmbFileSystemConnection fileSystem) {
    return fileSystem.validateConnection();
  }

  public void setClientFactory(SmbClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

  private String getErrorMessage(String message) {
    return format(SMB_ERROR_MESSAGE_MASK, this.connectionSettings.getHost(), this.connectionSettings.getDomain(),
                  this.connectionSettings.getUsername(), this.connectionSettings.getShareRoot(), this.logLevel, message);
  }

  // This validation needs to be done because of the bug explained in MULE-15197
  @Override
  public void onReturn(SmbFileSystemConnection connection) {
    if (!connection.validateConnection().isValid()) {
      LOGGER.debug("Connection is not valid, it is destroyed and not returned to the pool.");
      throw new IllegalStateException("Connection that is being returned to the pool is invalid.");
    }
  }

  @Override
  public String getWorkingDir() {
    //Working Dir is not used for SMB connector
    throw new RuntimeException("workingDir property should not be used");
  }

  void setHost(String host) {
    this.connectionSettings.setHost(host);
  }

  void setShareRoot(String shareRoot) {
    this.connectionSettings.setShareRoot(shareRoot);
  }

  void setDomain(String domain) {
    this.connectionSettings.setDomain(domain);
  }

  void setUsername(String username) {
    this.connectionSettings.setUsername(username);
  }

  void setPassword(String password) {
    this.connectionSettings.setPassword(password);
  }
}
