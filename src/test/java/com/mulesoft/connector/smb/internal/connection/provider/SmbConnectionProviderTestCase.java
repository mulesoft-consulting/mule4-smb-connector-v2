/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.provider;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.mulesoft.connector.smb.SmbServer;
import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.internal.connection.SmbClientFactory;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import com.mulesoft.connector.smb.internal.extension.SmbConnector;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class SmbConnectionProviderTestCase extends AbstractMuleTestCase {

  private static final String HOST = "localhost";
  private static final int PORT = 445;
  private static final int TIMEOUT = 10;
  private static final String SHARE_ROOT = "share";

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Mock
  private SmbConnector config;

  @Mock
  private SMBClient smbj;

  @Mock
  private Connection connection;

  @Mock
  private Session session;

  @Mock
  private DiskShare share;

  private SmbConnectionProvider provider = new SmbConnectionProvider();


  @Before
  public void before() throws Exception {
    provider.setHost(HOST);
    provider.setUsername(SmbServer.USERNAME);

    provider.setClientFactory(new SmbClientFactory() {

      public SmbClient createInstance(String host, int port, String shareRoot, boolean dfsEnabled, LogLevel logLevel) {
        return new SmbClient(host, port, shareRoot, dfsEnabled, logLevel);
      }
    });

    when(smbj.connect(HOST, PORT)).thenReturn(connection);
    AuthenticationContext authContext =
        new AuthenticationContext(SmbServer.USERNAME, SmbServer.PASSWORD.toCharArray(), SmbServer.DOMAIN);
    when(connection.authenticate(authContext)).thenReturn(session);
    when(session.connectShare(SHARE_ROOT)).thenReturn(this.share);
  }

  @Test
  @Ignore
  public void simpleCredentials() throws Exception {
    provider.setPassword(SmbServer.PASSWORD);
    login();
    //TODO olamiral: define assertions
  }

  private void login() throws Exception {
    SmbFileSystemConnection fileSystem = provider.connect();
    SmbClient client = spy(fileSystem.getClient());
    assertThat(fileSystem.getBasePath(), is(""));

    verify(session).connectShare(SHARE_ROOT);

    verify(client, never()).list(anyString());
  }

}
