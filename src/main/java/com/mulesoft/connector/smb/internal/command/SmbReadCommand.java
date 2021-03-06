/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.command;

import com.mulesoft.connector.smb.api.SmbFileAttributes;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import com.mulesoft.connector.smb.internal.config.SmbConfiguration;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.command.ReadCommand;
import org.mule.extension.file.common.api.lock.NullUriLock;
import org.mule.extension.file.common.api.lock.UriLock;
import org.mule.extension.file.common.api.util.UriUtils;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;
import java.net.URI;

import static org.mule.extension.file.common.api.util.UriUtils.createUri;

/**
 * A {@link SmbCommand} which implements the {@link ReadCommand} contract
 *
 * @since 1.0
 */
public final class SmbReadCommand extends SmbCommand implements ReadCommand<SmbFileAttributes> {

  public SmbReadCommand(SmbFileSystemConnection fileSystem, SmbClient client) {
    super(fileSystem, client);
  }

  /**
   * {@inheritDoc}
   * @deprecated
   * Inherided deprecated method from ReadCommand interface
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

  public SmbFileAttributes readAttributes(String filePath) {
    return getFile(filePath);
  }

  private Result<InputStream, SmbFileAttributes> read(FileConnectorConfig config, SmbFileAttributes attributes, boolean lock,
                                                      Long timeBetweenSizeCheck, boolean useCurrentConnection) {
    URI uri = UriUtils.createUri(attributes.getPath());

    UriLock pathLock = lock ? fileSystem.lock(uri) : new NullUriLock(uri);
    InputStream payload = null;
    try {
      payload = getFileInputStream((SmbConfiguration) config, attributes, pathLock, timeBetweenSizeCheck, useCurrentConnection);
      MediaType resolvedMediaType = fileSystem.getFileMessageMediaType(attributes);
      return Result.<InputStream, SmbFileAttributes>builder().output(payload).mediaType(resolvedMediaType).attributes(attributes)
          .build();
    } catch (Exception e) {
      IOUtils.closeQuietly(payload);
      throw exception("Could not fetch file " + uri.getPath(), e);
    }
  }

  private InputStream getFileInputStream(SmbConfiguration config, SmbFileAttributes attributes, UriLock pathLock,
                                         Long timeBetweenSizeCheck, boolean useCurrentConnection) {
    if (useCurrentConnection) {
      return SmbInputStream.newInstance(fileSystem, attributes, pathLock, timeBetweenSizeCheck);
    } else {
      return SmbInputStream.newInstance(config, attributes, pathLock, timeBetweenSizeCheck);
    }
  }
}
