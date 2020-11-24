/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package org.mule.extension.smb;

import org.mule.extension.smb.internal.utils.SmbUtils;
import org.mule.extension.smb.internal.connection.SmbClient;
import org.mule.extension.smb.internal.connection.SmbClientFactory;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

@ArtifactClassLoaderRunnerConfig(exportPluginClasses = {SmbClientFactory.class, SmbClient.class, SmbUtils.class})
public abstract class AbstractSmbConnectorTestCase extends MuleArtifactFunctionalTestCase {

}
