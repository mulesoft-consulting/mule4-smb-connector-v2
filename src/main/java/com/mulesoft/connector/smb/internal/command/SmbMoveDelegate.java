/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.command;

import com.mulesoft.connector.smb.internal.connection.FileCopyMode;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;

import java.net.URI;

import static java.lang.String.format;

public class SmbMoveDelegate implements SmbCopyDelegate {

  private final SmbCommand command;
  private final SmbFileSystemConnection fileSystem;

  public SmbMoveDelegate(SmbCommand command, SmbFileSystemConnection fileSystem) {
    this.command = command;
    this.fileSystem = fileSystem;
  }

  @Override
  public void doCopy(FileConnectorConfig config, FileAttributes source, URI targetUri, boolean overwrite) {
    try {
      if (command.exists(targetUri)) {
        fileSystem.delete(targetUri.getPath());
      }

      command.rename(source.getPath(), targetUri.getPath(), overwrite);
    } catch (Exception e) {
      throw command.exception(format("Found exception copying file '%s' to '%s'", source.getPath(), targetUri.getPath()), e);
    }
  }

  @Override
  public String getOperation() {
    return FileCopyMode.MOVE.label();
  }
}
