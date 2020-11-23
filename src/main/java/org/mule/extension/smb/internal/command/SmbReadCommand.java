/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.smb.internal.command;

import static org.mule.extension.file.common.api.util.UriUtils.createUri;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.command.ReadCommand;
import org.mule.extension.file.common.api.lock.NullUriLock;
import org.mule.extension.file.common.api.lock.UriLock;
import org.mule.extension.file.common.api.util.UriUtils;
import org.mule.extension.smb.api.SmbFileAttributes;
import org.mule.extension.smb.internal.extension.SmbConnector;
import org.mule.extension.smb.internal.connection.SmbClient;
import org.mule.extension.smb.internal.connection.SmbFileSystem;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.slf4j.Logger;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * A {@link SmbCommand} which implements the {@link ReadCommand} contract
 *
 * @since 1.0
 */
public final class SmbReadCommand extends SmbCommand implements ReadCommand<SmbFileAttributes> {

	private static final Logger LOGGER = getLogger(SmbReadCommand.class);

	/**
	 * {@inheritDoc}
	 */
	public SmbReadCommand(SmbFileSystem fileSystem, SmbClient client) {
		super(fileSystem, client);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public Result<InputStream, SmbFileAttributes> read(FileConnectorConfig config, String filePath, boolean lock) {
		return read(config, filePath, lock, null);
	}

	@Override
	public Result<InputStream, SmbFileAttributes> read(FileConnectorConfig config, String filePath, boolean lock,
														Long timeBetweenSizeCheck) {
		SmbFileAttributes attributes = getExistingFile(filePath);
		if (attributes.isDirectory()) {
			throw cannotReadDirectoryException(createUri(attributes.getPath()));
		}

		return read(config, attributes, lock, timeBetweenSizeCheck, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Result<InputStream, SmbFileAttributes> read(FileConnectorConfig config, SmbFileAttributes attributes, boolean lock,
														Long timeBetweenSizeCheck) {
		return read(config, attributes, lock, timeBetweenSizeCheck, false);
	}

	@Deprecated
	public Result<InputStream, SmbFileAttributes> read(FileConnectorConfig config, String filePath, boolean lock,
														Long timeBetweenSizeCheck, TimeUnit timeBetweenSizeCheckUnit) {
		return read(config, filePath, lock,
				config.getTimeBetweenSizeCheckInMillis(timeBetweenSizeCheck, timeBetweenSizeCheckUnit).orElse(null));
	}

	public SmbFileAttributes readAttributes(String filePath) {
		return getFile(filePath);
	}

	private Result<InputStream, SmbFileAttributes> read(FileConnectorConfig config, SmbFileAttributes attributes, boolean lock,
														 Long timeBetweenSizeCheck, boolean useCurrentConnection) {
		URI uri = UriUtils.createUri(attributes.getPath());

		UriLock pathLock = lock ? fileSystem.lock(uri) : new NullUriLock(uri);
		InputStream payload = null;
		try {
			payload = getFileInputStream((SmbConnector) config, attributes, pathLock, timeBetweenSizeCheck, useCurrentConnection);
			MediaType resolvedMediaType = fileSystem.getFileMessageMediaType(attributes);
			return Result.<InputStream, SmbFileAttributes>builder().output(payload).mediaType(resolvedMediaType).attributes(attributes)
					.build();
		} catch (Exception e) {
			IOUtils.closeQuietly(payload);
			throw exception("Could not fetch file " + uri.getPath(), e);
		} finally {
			if (pathLock != null) {
				try {
					pathLock.release();
				} catch(Exception e) {
					LOGGER.warn("Could not release lock for path " + uri.toString(), e);
				}
			}
		}
	}

	private InputStream getFileInputStream(SmbConnector config, SmbFileAttributes attributes, UriLock pathLock,
										   Long timeBetweenSizeCheck, boolean useCurrentConnection)
			throws ConnectionException {
		if (useCurrentConnection) {
			return SmbInputStream.newInstance(fileSystem, attributes, pathLock, timeBetweenSizeCheck);
		} else {
			return SmbInputStream.newInstance(config, attributes, pathLock, timeBetweenSizeCheck);
		}
	}
}
