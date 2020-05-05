/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.smb.internal.command;

import static java.lang.String.format;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.smb.internal.connection.SmbFileSystem;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.net.URI;

public class SmbMoveDelegate implements SmbCopyDelegate {

    private SmbCommand command;
    private SmbFileSystem fileSystem;

    public SmbMoveDelegate(SmbCommand command, SmbFileSystem fileSystem) {
        this.command = command;
        this.fileSystem = fileSystem;
    }

    @Override
    public void doCopy(FileConnectorConfig config, FileAttributes source, URI targetUri, boolean overwrite) {
        try {
            if (command.exists(targetUri)) {
                if (overwrite) {
                    fileSystem.delete(targetUri.getPath());
                } else {
                    command.alreadyExistsException(targetUri);
                }
            }

            command.rename(source.getPath(), targetUri.getPath(), overwrite);
        } catch (ModuleException e) {
            throw e;
        } catch (Exception e) {
            throw command.exception(format("Found exception copying file '%s' to '%s'", source.getPath(), targetUri.getPath()), e);
        }
    }
}
