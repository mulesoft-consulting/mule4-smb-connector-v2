/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;

import com.hierynomus.smbj.SMBClient;
import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import org.mule.tck.size.SmallTest;

import java.net.URI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class SmbClientTestCase {

  private static final String FILE_PATH = "/bla/file.txt";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private URI uri = createUri(FILE_PATH);

  @InjectMocks
  private SmbClient client = new SmbClient(EMPTY, 0, EMPTY, false, LogLevel.WARN);

  @Test
  public void returnNullOnUnexistingFile() throws Exception {
    //TODO olamiral: implement
    /*
    when(channel.stat(any())).thenThrow(new SmbException(SSH_FX_NO_SUCH_FILE, "No such file"));
    assertThat(client.getAttributes(uri), is(nullValue()));
     */
  }

  @Test
  public void exceptionIsThrownOnError() throws Exception {
    //TODO olamiral: implement
    /*
    expectedException.expect(MuleRuntimeException.class);
    expectedException.expectMessage(format("Could not obtain attributes for path %s", FILE_PATH));
    when(channel.stat(any())).thenThrow(new SmbException(SSH_FX_PERMISSION_DENIED, EMPTY));
    client.getAttributes(uri);
     */
  }

  @Test
  public void expectConnectionExceptionWhenIOExceptionIsThrown() throws Exception {
    //TODO olamiral: implement
    /*
    expectedException.expect(MuleRuntimeException.class);
    expectedException.expectCause(instanceOf(ConnectionException.class));
    when(channel.stat(any())).thenThrow(new SmbException(SSH_FX_PERMISSION_DENIED, EMPTY, new IOException()));
    client.getAttributes(uri);
     */
  }
}
