/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.command;

import org.mule.extension.file.common.api.command.RenameCommand;
import com.mulesoft.connector.smb.internal.connection.SmbClient;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;

/**
 * A {@link SmbCommand} which implements the {@link RenameCommand} contract
 *
 * @since 1.0
 */
public final class SmbRenameCommand extends SmbCommand implements RenameCommand {

	/**
	 * {@inheritDoc}
	 */
	public SmbRenameCommand(SmbFileSystemConnection fileSystem, SmbClient client) {
		super(fileSystem, client);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rename(String filePath, String newName, boolean overwrite) {
		super.rename(filePath, newName, overwrite);
	}
}
