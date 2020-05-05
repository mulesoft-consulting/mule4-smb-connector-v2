/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.smb.internal.command;

import org.mule.extension.file.common.api.command.RenameCommand;
import org.mule.extension.smb.internal.connection.SmbClient;
import org.mule.extension.smb.internal.connection.SmbFileSystem;

/**
 * A {@link SmbCommand} which implements the {@link RenameCommand} contract
 *
 * @since 1.0
 */
public final class SmbRenameCommand extends SmbCommand implements RenameCommand {

	/**
	 * {@inheritDoc}
	 */
	public SmbRenameCommand(SmbFileSystem fileSystem, SmbClient client) {
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
