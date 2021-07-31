/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.connection;

import com.mulesoft.connector.smb.internal.connection.client.SmbClient;

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
   * @return a {@link SmbClient}
   */
  public SmbClient createInstance(String host, int port, String shareRoot, boolean dfsEnabled) {
    return new SmbClient(host, port, shareRoot, dfsEnabled);
  }
}
