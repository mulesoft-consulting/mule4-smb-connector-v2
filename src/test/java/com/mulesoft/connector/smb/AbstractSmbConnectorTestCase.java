/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb;

import com.mulesoft.connector.smb.internal.utils.SmbUtils;
import com.mulesoft.connector.smb.internal.connection.SmbClient;
import com.mulesoft.connector.smb.internal.connection.SmbClientFactory;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

@ArtifactClassLoaderRunnerConfig(exportPluginClasses = {SmbClientFactory.class, SmbClient.class, SmbUtils.class})
public abstract class AbstractSmbConnectorTestCase extends MuleArtifactFunctionalTestCase {

}
