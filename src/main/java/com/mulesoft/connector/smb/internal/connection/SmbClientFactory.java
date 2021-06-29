/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection;

import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.internal.connection.client.impl.smbj.SmbjSmbClient;
import com.mulesoft.connector.smb.internal.connection.provider.TimeoutSettings;

/**
 * Creates instances of {@link SmbClient}
 *
 * @since 1.0
 */
public class SmbClientFactory {

  /**
   * Creates a new instance which will connect to the given {@code host}. {@code shareRoot} will be used as the base path.
   *
   * @param host the host address
   * @param shareRoot the share root
   * @param logLevel the log level
   * @return a {@link SmbClient}
   */
  public SmbClient createInstance(String host, int port, String shareRoot, LogLevel logLevel, boolean dfsEnabled,
                                  TimeoutSettings timeoutSettings) {
    SmbClient result = new SmbjSmbClient(host, port, shareRoot, logLevel, dfsEnabled);
    if (timeoutSettings != null) {

      if (timeoutSettings.getConnectionTimeout() != null && timeoutSettings.getConnectionTimeoutUnit() != null) {
        result.setConnectionTimeout(timeoutSettings.getConnectionTimeoutUnit(), timeoutSettings.getConnectionTimeout());
      }

      if (timeoutSettings.getSocketTimeout() != null && timeoutSettings.getSocketTimeoutUnit() != null) {
        result.setSocketTimeout(timeoutSettings.getSocketTimeoutUnit(), timeoutSettings.getSocketTimeout());
      }

      if (timeoutSettings.getReadTimeout() != null && timeoutSettings.getReadTimeoutUnit() != null) {
        result.setReadTimeout(timeoutSettings.getReadTimeoutUnit(), timeoutSettings.getReadTimeout());
      }

      if (timeoutSettings.getTransactionTimeout() != null && timeoutSettings.getTransactionTimeoutUnit() != null) {
        result.setTransactionTimeout(timeoutSettings.getTransactionTimeoutUnit(), timeoutSettings.getTransactionTimeout());
      }

      if (timeoutSettings.getWriteTimeout() != null && timeoutSettings.getWriteTimeoutUnit() != null) {
        result.setWriteTimeout(timeoutSettings.getWriteTimeoutUnit(), timeoutSettings.getWriteTimeout());
      }


    }
    return result;
  }
}
