/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.command;

import static java.lang.String.format;

import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileWriteMode;
import com.mulesoft.connector.smb.internal.extension.SmbConnector;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionHandler;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

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
    public AbstractSmbCopyDelegate(SmbCommand command, SmbFileSystemConnection fileSystem) {
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
        ConnectionHandler<SmbFileSystemConnection> writerConnectionHandler;
        final SmbFileSystemConnection writerConnection;
        try {
            writerConnectionHandler = getWriterConnection(config);
            writerConnection = writerConnectionHandler.getConnection();
        } catch (ConnectionException e) {
            throw command
                    .exception(format("SMB Copy operations require the use of two SMB connections. An exception was found trying to obtain second connection to"
                            + "copy the path '%s' to '%s'", source.getPath(), targetUri.getPath()), e);
        }
        try {
            if (source.isDirectory()) {
                copyDirectory(config, URI.create(source.getPath()), targetUri, overwrite, writerConnection);
            } else {
                copyFile(config, source, targetUri, overwrite, writerConnection);
            }
        } catch (ModuleException e) {
            throw e;
        } catch (Exception e) {
            throw command.exception(format("Found exception copying file '%s' to '%s'", source, targetUri.getPath()), e);
        } finally {
            writerConnectionHandler.release();
        }
    }

    /**
     * Performs a recursive copy of a directory
     *  @param config the config which is parameterizing this operation
     * @param sourceUri the path to the directory to be copied
     * @param target the target path
     * @param overwrite whether to overwrite the target files if they already exists
     * @param writerConnection the {@link SmbFileSystemConnection} which connects to the target endpoint
     */
    protected abstract void copyDirectory(FileConnectorConfig config, URI sourceUri, URI target, boolean overwrite,
                                          SmbFileSystemConnection writerConnection);

    /**
     * Copies one individual file
     *  @param config the config which is parameterizing this operation
     * @param source the {@link FileAttributes} for the file to be copied
     * @param target the target uri
     * @param overwrite whether to overwrite the target files if they already exists
     * @param writerConnection the {@link SmbFileSystemConnection} which connects to the target endpoint
     */
    protected void copyFile(FileConnectorConfig config, FileAttributes source, URI target, boolean overwrite,
                            SmbFileSystemConnection writerConnection) {
        // FIXME olamiral: assume target is not resolved
        FileAttributes targetFile = command.getFile(target.toString());
        if (targetFile != null) {
            if (overwrite) {
                fileSystem.delete(targetFile.getPath());
            } else {
                throw command.alreadyExistsException(target);
            }
        }

        try (InputStream inputStream = fileSystem.retrieveFileContent(source)) {
            if (inputStream == null) {
                throw command
                        .exception(format("Could not read file '%s' while trying to copy it to remote path '%s'", source.getPath(), target));
            }

            writeCopy(config, target.getPath(), inputStream, overwrite, writerConnection);
        } catch (Exception e) {
            throw command
                    .exception(format("Found exception while trying to copy file '%s' to remote path '%s'", source.getPath(), target), e);
        }
    }

    private void writeCopy(FileConnectorConfig config, String targetPath, InputStream inputStream, boolean overwrite,
                           SmbFileSystemConnection writerConnection)
            throws IOException {
        final FileWriteMode mode = overwrite ? FileWriteMode.OVERWRITE : FileWriteMode.CREATE_NEW;
        writerConnection.write(targetPath, inputStream, mode, false, true);
    }

    private ConnectionHandler<SmbFileSystemConnection> getWriterConnection(FileConnectorConfig config) throws ConnectionException {
        return ((SmbConnector) config).getConnectionManager().getConnection(config);
    }
}
