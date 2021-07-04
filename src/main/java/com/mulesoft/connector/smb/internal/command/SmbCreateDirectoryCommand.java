/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.command;

import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import org.mule.extension.file.common.api.command.CreateDirectoryCommand;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;

/**
 * A {@link SmbCommand} which implements the {@link CreateDirectoryCommand} contract
 *
 * @since 1.0
 */
public final class SmbCreateDirectoryCommand extends SmbCommand implements CreateDirectoryCommand {

  public SmbCreateDirectoryCommand(SmbFileSystemConnection fileSystem, SmbClient client) {
    super(fileSystem, client);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createDirectory(String directoryName) {
    super.createDirectory(directoryName);
  }
}
