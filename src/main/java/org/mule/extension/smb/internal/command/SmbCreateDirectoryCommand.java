/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.smb.internal.command;

import org.mule.extension.file.common.api.command.CreateDirectoryCommand;
import org.mule.extension.smb.internal.connection.SmbClient;
import org.mule.extension.smb.internal.connection.SmbFileSystem;

/**
 * A {@link SmbCommand} which implements the {@link CreateDirectoryCommand} contract
 *
 * @since 1.0
 */
public final class SmbCreateDirectoryCommand extends SmbCommand implements CreateDirectoryCommand {

  /**
   * {@inheritDoc}
   */
  public SmbCreateDirectoryCommand(SmbFileSystem fileSystem, SmbClient client) {
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
