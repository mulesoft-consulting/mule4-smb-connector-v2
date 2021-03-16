/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.source;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import com.mulesoft.connector.smb.internal.command.SmbCommand;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;

import java.net.URI;

/**
 * A {@link SmbCommand} which implements support functionality for
 * {@link SmbDirectorySource}
 *
 * @since 1.1
 */
public class OnNewFileCommand extends SmbCommand {

	OnNewFileCommand(SmbFileSystemConnection fileSystem) {
		super(fileSystem);
	}

	/**
	 * Resolves the root path on which the listener needs to be created
	 *
	 * @param directory the path that the user configured on the listener
	 * @return the resolved {@link URI} to listen on
	 */
	public URI resolveRootPath(String directory) {
		return resolveExistingPath(directory);
	}
}
