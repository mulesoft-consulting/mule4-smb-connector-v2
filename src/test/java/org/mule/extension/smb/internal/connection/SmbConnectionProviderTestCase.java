/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package org.mule.extension.smb.internal.connection;

import org.junit.Before;
import org.mule.extension.smb.api.LogLevel;
import org.mule.extension.smb.internal.connection.provider.SmbConnectionProvider;
import org.mule.tck.junit4.AbstractMuleTestCase;

public class SmbConnectionProviderTestCase  extends AbstractMuleTestCase {

    private String host;
    private String shareRoot;
    private String domain;
    private String username;
    private String password;

    private SmbConnectionProvider provider = new SmbConnectionProvider();

    @Before
    public void before() throws Exception {
        provider.setHost("localhost");
        provider.setShareRoot("share");
        provider.setDomain("WORKGROUP");
        provider.setUsername("mulesoft");
        provider.setPassword("mulesoft");

        provider.setClientFactory(new SmbClientFactory() {

            @Override
            public SmbClient createInstance(String host, String shareRoot, LogLevel logLevel) {
                return new SmbClient(host, shareRoot, logLevel);
            }
        });
    }

}
