/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.command;

import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;

import java.net.URI;

/**
 * A delegate object for copying files
 *
 * @since 1.0
 */
@FunctionalInterface
public interface SmbCopyDelegate {

    /**
     * Performs the copy operation
     * @param config the config which is parameterizing this operation
     * @param source the attributes which describes the source file
     * @param targetUri the target uri
     * @param overwrite whether to overwrite the target file if it already exists
     */
    void doCopy(FileConnectorConfig config, FileAttributes source, URI targetUri, boolean overwrite);
}
