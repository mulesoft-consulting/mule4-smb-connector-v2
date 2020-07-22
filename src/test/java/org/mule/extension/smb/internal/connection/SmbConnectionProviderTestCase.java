package org.mule.extension.smb.internal.connection;

import jcifs.smb.SmbFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mule.extension.smb.api.LogLevel;
import org.mule.extension.smb.internal.SmbConnectionProvider;
import org.mule.extension.smb.internal.connection.SmbClient;
import org.mule.extension.smb.internal.connection.SmbClientFactory;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.tck.junit4.AbstractMuleTestCase;

import java.net.URI;

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
