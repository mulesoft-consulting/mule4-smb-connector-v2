/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.command;

import static org.mule.extension.file.common.api.util.UriUtils.createUri;

import com.mulesoft.connector.smb.internal.connection.FileCopyMode;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.command.CopyCommand;

import java.net.URI;

/**
 * A {@link SmbCommand} which implements the {@link CopyCommand} contract
 *
 * @since 1.0
 */
public class SmbCopyCommand extends SmbCommand implements CopyCommand {

  public SmbCopyCommand(SmbFileSystemConnection fileSystem, SmbClient client) {
    super(fileSystem, client);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void copy(FileConnectorConfig config, String sourcePath, String targetPath, boolean overwrite,
                   boolean createParentDirectories, String renameTo) {
    copy(config, sourcePath, targetPath, overwrite, createParentDirectories, renameTo,
         new SmbCopyDelegate(this, this.fileSystem));
  }

  private class SmbCopyDelegate extends AbstractSmbCopyDelegate {

    public SmbCopyDelegate(SmbCommand command, SmbFileSystemConnection fileSystem) {
      super(command, fileSystem);
    }

    @Override
    protected void copyDirectory(URI sourceUri, URI target, boolean overwrite) {
      for (FileAttributes fileAttributes : client.list(sourceUri.toString())) {
        URI targetUri = createUri(target.getPath(), fileAttributes.getName());
        if (fileAttributes.isDirectory()) {
          copyDirectory(URI.create(fileAttributes.getPath()), targetUri, overwrite);
        } else {
          copyFile(fileAttributes, targetUri, overwrite);
        }
      }
    }

    @Override
    public String getOperation() {
      return FileCopyMode.COPY.label();
    }
  }
}
