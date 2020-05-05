package org.mule.extension.smb.internal.source;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import org.mule.extension.smb.internal.command.SmbCommand;
import org.mule.extension.smb.internal.connection.SmbFileSystem;

import java.net.URI;

/**
 * A {@link SmbCommand} which implements support functionality for
 * {@link SmbDirectoryListener}
 *
 * @since 1.1
 */
public class OnNewFileCommand extends SmbCommand {

	OnNewFileCommand(SmbFileSystem fileSystem) {
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
