/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.command;

import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.command.MoveCommand;
import com.mulesoft.connector.smb.internal.connection.SmbClient;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SmbCommand} which implements the {@link MoveCommand} contract
 *
 * @since 1.0
 */
public class SmbMoveCommand extends SmbCommand implements MoveCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmbMoveCommand.class);

	/**
	 * {@inheritDoc}
	 */
	public SmbMoveCommand(SmbFileSystemConnection fileSystem, SmbClient client) {
		super(fileSystem, client);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void move(FileConnectorConfig config, String sourcePath, String targetPath, boolean overwrite,
					 boolean createParentDirectories, String renameTo) {
		copy(config, sourcePath, targetPath, overwrite, createParentDirectories, renameTo, new SmbMoveDelegate(this, fileSystem));
		LOGGER.debug("Moved '{}' to '{}'", sourcePath, targetPath);
	}
}
