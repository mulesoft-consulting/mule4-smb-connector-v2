/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.command;

import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileWriteMode;

import java.io.InputStream;
import java.net.URI;

import static java.lang.String.format;

/**
 * Abstract implementation of {@link SmbCopyDelegate} for copying operations which require to SMB connections, one for reading the
 * source file and another for writing into the target path
 *
 * @since 1.0
 */
public abstract class AbstractSmbCopyDelegate implements SmbCopyDelegate {

  private final SmbCommand command;
  private final SmbFileSystemConnection fileSystem;

  /**
   * Creates new instance
   *
   * @param command the {@link SmbCommand} which requested this operation
   * @param fileSystem the {@link SmbFileSystemConnection} which connects to the remote server
   */
  protected AbstractSmbCopyDelegate(SmbCommand command, SmbFileSystemConnection fileSystem) {
    this.command = command;
    this.fileSystem = fileSystem;
  }

  /**
   * Performs a recursive copy
   *  @param config the config which is parameterizing this operation
   * @param source the {@link FileAttributes} for the file to be copied
   * @param targetUri the {@link URI} to the target destination
   * @param overwrite whether to overwrite existing target paths
   */
  @Override
  public void doCopy(FileConnectorConfig config, FileAttributes source, URI targetUri, boolean overwrite) {
    if (source.isDirectory()) {
      copyDirectory(URI.create(source.getPath()), targetUri, overwrite);
    } else {
      copyFile(source, targetUri, overwrite);
    }
  }

  /**
   * Performs a recursive copy of a directory
   * @param sourceUri the path to the directory to be copied
   * @param target the target path
   * @param overwrite whether to overwrite the target files if they already exists
   */
  protected abstract void copyDirectory(URI sourceUri, URI target, boolean overwrite);

  /**
   * Copies one individual file
   * @param source the {@link FileAttributes} for the file to be copied
   * @param target the target uri
   * @param overwrite whether to overwrite the target files if they already exists
   */
  protected void copyFile(FileAttributes source, URI target, boolean overwrite) {
    if (command.exists(target)) {
      fileSystem.delete(target.getPath());
    }

    try (InputStream inputStream = fileSystem.retrieveFileContent(source)) {
      writeCopy(target.getPath(), inputStream, overwrite);
    } catch (Exception e) {
      throw command
          .exception(format("Found exception while trying to copy file '%s' to remote path '%s'", source.getPath(), target), e);
    }
  }

  private void writeCopy(String targetPath, InputStream inputStream, boolean overwrite) {
    final FileWriteMode mode = overwrite ? FileWriteMode.OVERWRITE : FileWriteMode.CREATE_NEW;
    fileSystem.write(targetPath, inputStream, mode, false, true);
  }

}
