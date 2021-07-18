/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.provider;

import com.mulesoft.connector.smb.internal.connection.SmbClientFactory;
import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import com.mulesoft.connector.smb.internal.error.exception.SmbConnectionException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mule.extension.file.common.api.exceptions.FileError.CONNECTION_TIMEOUT;
import static org.mule.extension.file.common.api.exceptions.FileError.UNKNOWN_HOST;

//@RunWith(MockitoJUnitRunner.class)
public class SmbConnectionProviderTestCase {

  private static final String HOST = "localhost";
  private static final int PORT = 445;
  private static final int TIMEOUT = 10;
  private static final String SHARE_ROOT = "share";

  protected static final String NAMESPACE = "SMB";

  private static final Matcher<Exception> SMB_CONNECTION_EXCEPTION =
      is(CoreMatchers.instanceOf(SmbConnectionException.class));

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void disconnectWithNullFileSystemConnection() {
    expectedException.expect(NullPointerException.class);

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.disconnect(null);
  }

  @Test
  public void validateConnectionWithNullFileSystemConnection() {
    expectedException.expect(NullPointerException.class);

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.validate(null);
  }

  @Test
  public void verifyGetWorkingDir() {
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("workingDir property should not be used");

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.getWorkingDir();
  }

  @Test
  public void verifySocketTimeoutExceptionWhenConnecting() throws Exception {
    expectedException.expect(SmbConnectionException.class);
    SmbConnectionProvider provider = new SmbConnectionProvider();
    SmbClient mockedClient = mock(SmbClient.class);
    doThrow(new SocketTimeoutException("connect timed out")).when(mockedClient).login(anyString(), anyString());
    provider.setClientFactory(new SmbClientFactory() {

      @Override
      public SmbClient createInstance(String host, int port, String shareRoot, boolean dfsEnabled) {
        return mockedClient;
      }
    });
    try {
      provider.connect();
    } catch (SmbConnectionException sce) {
      verifyConnectionException(sce, SocketTimeoutException.class, "connect timed out", CONNECTION_TIMEOUT);
    }
  }

  @Test
  public void verifyUnknownHostExceptionWhenConnecting() throws Exception {
    expectedException.expect(SmbConnectionException.class);
    SmbConnectionProvider provider = new SmbConnectionProvider();
    SmbClient mockedClient = mock(SmbClient.class);
    doThrow(new UnknownHostException("unknown host")).when(mockedClient).login(anyString(), anyString());
    provider.setClientFactory(new SmbClientFactory() {

      @Override
      public SmbClient createInstance(String host, int port, String shareRoot, boolean dfsEnabled) {
        return mockedClient;
      }
    });
    try {
      provider.connect();
    } catch (SmbConnectionException sce) {
      verifyConnectionException(sce, UnknownHostException.class, "unknown host", UNKNOWN_HOST);
    }
  }

  private void verifyConnectionException(SmbConnectionException sce, Class<? extends Throwable> throwableClass,
                                         String expectedMessage, FileError expectedFileError)
      throws SmbConnectionException {
    assertThat(sce.getMessage(), containsString(expectedMessage));
    assertThat(sce.getCause(), instanceOf(ModuleException.class));
    ModuleException me = (ModuleException) sce.getCause();
    assertThat(me.getCause(), instanceOf(throwableClass));
    assertThat(me.getType(), is(expectedFileError));
    throw sce;
  }

  @Test
  public void verifyExceptionWhenConnecting() throws Exception {
    expectedException.expect(ConnectionException.class);
    expectedException.expectCause(instanceOf(RuntimeException.class));
    expectedException.expectMessage("Error occurred");

    SmbConnectionProvider provider = new SmbConnectionProvider();
    SmbClient mockedClient = mock(SmbClient.class);
    doThrow(new RuntimeException("Error occurred")).when(mockedClient).login(anyString(), anyString());
    provider.setClientFactory(new SmbClientFactory() {

      @Override
      public SmbClient createInstance(String host, int port, String shareRoot, boolean dfsEnabled) {
        return mockedClient;
      }
    });
    provider.connect();
  }

}
