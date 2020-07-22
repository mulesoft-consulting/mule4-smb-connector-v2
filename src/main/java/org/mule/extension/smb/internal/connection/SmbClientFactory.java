package org.mule.extension.smb.internal.connection;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import org.mule.extension.smb.api.LogLevel;

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
  public SmbClient createInstance(String host, String shareRoot, LogLevel logLevel) {
    return new SmbClient(host, shareRoot, logLevel);
  }
}
