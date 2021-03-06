/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.command;

import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.command.DeleteCommand;
import org.slf4j.Logger;

import static org.mule.extension.file.common.api.util.UriUtils.createUri;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A {@link SmbCommand} which implements the {@link DeleteCommand} contract
 *
 * @since 1.0
 */
public final class SmbDeleteCommand extends SmbCommand implements DeleteCommand {

  private static final Logger logger = getLogger(SmbDeleteCommand.class);

  public SmbDeleteCommand(SmbFileSystemConnection fileSystem, SmbClient client) {
    super(fileSystem, client);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(String filePath) {
    FileAttributes fileAttributes = getExistingFile(filePath);
    final boolean isDirectory = fileAttributes.isDirectory();
    final String path = fileAttributes.getPath();

    if (isDirectory) {
      deleteDirectory(path);
    } else {
      deleteFile(path);
    }
  }

  private void deleteFile(String path) {
    fileSystem.verifyNotLocked(createUri(path));
    logger.debug("Preparing to delete file '{}'", path);
    client.delete(path);

    logDelete(path);
  }

  private void deleteDirectory(String path) {
    logger.debug("Preparing to delete directory '{}'", path);
    String actualPath = path + (path.endsWith("/") ? "" : "/");
    for (FileAttributes file : client.list(path)) {
      final String filePath = file.getPath();

      if (file.isDirectory()) {
        deleteDirectory(filePath);
      } else {
        deleteFile(filePath);
      }
    }

    if (!client.pathIsShareRoot(actualPath)) {
      client.delete(actualPath);
      logDelete(path);
    }
  }

  private void logDelete(String path) {
    logger.debug("Successfully deleted '{}'", path);
  }
}
