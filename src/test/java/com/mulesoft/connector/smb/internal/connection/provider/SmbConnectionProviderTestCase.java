/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.provider;

import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.internal.connection.SmbClient;
import com.mulesoft.connector.smb.internal.connection.SmbClientFactory;
import com.mulesoft.connector.smb.internal.connection.client.impl.smbj.SmbjSmbClient;
import com.mulesoft.connector.smb.internal.connection.provider.SmbConnectionProvider;
import com.mulesoft.connector.smb.internal.connection.provider.TimeoutSettings;
import org.junit.Before;
import org.mule.tck.junit4.AbstractMuleTestCase;

public class SmbConnectionProviderTestCase extends AbstractMuleTestCase {

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
    provider.setClientFactory(new SmbClientFactory());
  }

}
