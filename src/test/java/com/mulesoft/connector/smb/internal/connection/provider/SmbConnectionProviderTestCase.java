/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.provider;

import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.protocol.transport.TransportException;
import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.internal.connection.SmbClientFactory;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import com.mulesoft.connector.smb.internal.error.exception.SmbConnectionException;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.extension.file.common.api.FileWriteMode;
import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mule.extension.file.common.api.exceptions.FileError.*;

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
  public void connectWithNullShareRoot() throws Exception {
    expectedException.expect(ConnectionException.class);
    expectedException.expectCause(instanceOf(IllegalArgumentException.class));
    expectedException.expectMessage("shareRoot is null");

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.connect();
  }

  @Test
  public void connectWithNullHost() throws Exception {
    expectedException.expect(ConnectionException.class);
    expectedException.expectCause(instanceOf(IllegalArgumentException.class));
    expectedException.expectMessage("hostname can't be null");

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setShareRoot("invalid");
    provider.connect();
  }

  @Test
  public void connectToUnknownHost() throws Exception {
    expectedException.expect(SmbConnectionException.class);
    expectedException.expectCause(instanceOf(ModuleException.class));
    expectedException.expectMessage("Could not establish SMB connection");
    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("somehostname");
    provider.setShareRoot("invalid");
    provider.connect();
  }

  @Test
  public void connectUsingInvalidPort() throws Exception {
    expectedException.expect(SmbConnectionException.class);

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setShareRoot("invalid");

    try {
      provider.connect();
    } catch (SmbConnectionException sce) {
      verifyConnectionException(sce, ConnectException.class, "Can't assign requested address (connect failed)", CANNOT_REACH);
    }
  }

  @Test
  public void connectToInvalidServer() throws Exception {
    expectedException.expect(SmbConnectionException.class);
    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(446);
    provider.setShareRoot("invalid");
    try {
      provider.connect();
    } catch (SmbConnectionException sce) {
      verifyConnectionException(sce, ConnectException.class, "Connection refused", CANNOT_REACH);
    }
  }

  @Test
  public void connectWithInvalidShareRoot() throws Exception {
    expectedException.expect(ConnectionException.class);
    expectedException.expectCause(instanceOf(SMBApiException.class));
    expectedException.expectMessage("STATUS_BAD_NETWORK_NAME");
    expectedException.expectMessage("Could not connect to \\\\localhost\\invalid");

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("invalid");
    provider.connect();
  }

  @Test
  public void connectWithNullUsername() throws Exception {
    expectedException.expect(SmbConnectionException.class);
    expectedException.expectCause(instanceOf(ModuleException.class));
    expectedException.expectMessage("STATUS_ACCESS_DENIED");
    expectedException.expectMessage("Could not connect to \\\\localhost\\share");

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("share");
    provider.connect();
  }

  @Test
  public void connectWithNullPassword() throws Exception {
    expectedException.expect(SmbConnectionException.class);
    expectedException.expectCause(instanceOf(ModuleException.class));
    expectedException.expectMessage("STATUS_ACCESS_DENIED");
    expectedException.expectMessage("Could not connect to \\\\localhost\\share");

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("share");
    provider.setUsername("invalid");
    provider.connect();
  }

  @Test
  public void connectWithInvalidCredentials() throws Exception {
    expectedException.expect(SmbConnectionException.class);
    expectedException.expectCause(instanceOf(ModuleException.class));
    expectedException.expectMessage("STATUS_ACCESS_DENIED");
    expectedException.expectMessage("Could not connect to \\\\localhost\\share");

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("share");
    provider.setUsername("invalid");
    provider.setPassword("invalid");
    provider.connect();
  }

  @Test
  public void connect() throws Exception {
    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("share");
    provider.setUsername("mulesoft");
    provider.setPassword("mulesoft");
    SmbFileSystemConnection fileSystemConnection = provider.connect();
    ConnectionValidationResult validationResult = provider.validate(fileSystemConnection);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void connectWithInvalidDomain() throws Exception {
    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("share");
    provider.setUsername("mulesoft");
    provider.setPassword("mulesoft");
    provider.setDomain("invalid");
    SmbFileSystemConnection fileSystemConnection = provider.connect();
    ConnectionValidationResult validationResult = provider.validate(fileSystemConnection);
    assertTrue(validationResult.isValid());
  }

  @Test
  public void disconnectWithNullFileSystemConnection() throws Exception {
    expectedException.expect(NullPointerException.class);

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.disconnect(null);
  }

  @Test
  public void disconnect() throws Exception {
    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("share");
    provider.setUsername("mulesoft");
    provider.setPassword("mulesoft");
    SmbFileSystemConnection fileSystemConnection = provider.connect();
    provider.disconnect(fileSystemConnection);
    ConnectionValidationResult validationResult = provider.validate(fileSystemConnection);
    assertFalse(validationResult.isValid());
    MatcherAssert.assertThat(validationResult.getException(), SMB_CONNECTION_EXCEPTION);
    assertEquals("Connection is stale", validationResult.getMessage());
  }

  @Test
  public void disconnectAlreadyDisconnected() throws Exception {
    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("share");
    provider.setUsername("mulesoft");
    provider.setPassword("mulesoft");
    SmbFileSystemConnection fileSystemConnection = provider.connect();
    provider.disconnect(fileSystemConnection);
    //TODO olamiral: verify if should throw exception or not
    provider.disconnect(fileSystemConnection);

    ConnectionValidationResult validationResult = provider.validate(fileSystemConnection);
    assertFalse(validationResult.isValid());
    MatcherAssert.assertThat(validationResult.getException(), SMB_CONNECTION_EXCEPTION);
    assertEquals("Connection is stale", validationResult.getMessage());

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
      public SmbClient createInstance(String host, int port, String shareRoot, boolean dfsEnabled, LogLevel logLevel) {
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
      public SmbClient createInstance(String host, int port, String shareRoot, boolean dfsEnabled, LogLevel logLevel) {
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
      public SmbClient createInstance(String host, int port, String shareRoot, boolean dfsEnabled, LogLevel logLevel) {
        return mockedClient;
      }
    });
    provider.connect();
  }

  @Test
  public void connectUsingNegativeSocketTimeout() throws ConnectionException {
    expectedException.expect(ConnectionException.class);
    expectedException.expectCause(instanceOf(IllegalArgumentException.class));
    expectedException.expectMessage("Socket timeout should be either 0 (no timeout) or a positive value");

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("share");
    provider.setUsername("mulesoft");
    provider.setPassword("mulesoft");
    provider.setSocketTimeout(TimeUnit.MILLISECONDS, -1);
    provider.connect();
  }

  @Test
  public void verifySocketTimeoutSettings() throws ConnectionException {
    expectedException.expect(ConnectionException.class);
    expectedException.expectCause(instanceOf(TransportException.class));
    expectedException.expectMessage("Read timed out");

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("share");
    provider.setUsername("mulesoft");
    provider.setPassword("mulesoft");
    provider.setSocketTimeout(TimeUnit.MILLISECONDS, 1);
    provider.connect();
  }


  @Test
  public void verifyTransactionTimeoutSettings() throws ConnectionException {
    expectedException.expect(ConnectionException.class);
    expectedException.expectCause(instanceOf(TransportException.class));
    expectedException.expectMessage("Timeout expired");

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("share");
    provider.setUsername("mulesoft");
    provider.setPassword("mulesoft");
    provider.setTransactionTimeout(TimeUnit.MILLISECONDS, -1);
    provider.connect();
  }

  @Test
  public void verifyWriteTimeoutSettings() throws ConnectionException, IOException {
    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("share");
    provider.setUsername("mulesoft");
    provider.setPassword("mulesoft");
    provider.setWriteTimeout(TimeUnit.MILLISECONDS, -1);
    provider.setReadTimeout(TimeUnit.MINUTES, 1);
    SmbFileSystemConnection fileSystemConnection = provider.connect();
    fileSystemConnection.getClient().write("test.txt", IOUtils.toInputStream("this is a test", Charset.defaultCharset()),
                                           FileWriteMode.OVERWRITE);
    InputStream fileContent = fileSystemConnection.getClient().read("test.txt");
    assertEquals("this is a test", IOUtils.toString(fileContent, Charset.defaultCharset()));
  }

  @Test
  public void verifyReadTimeoutSettings() throws ConnectionException, IOException {
    expectedException.expect(MuleRuntimeException.class);
    expectedException.expectCause(instanceOf(TransportException.class));
    expectedException.expectMessage("Cannot read from file");

    SmbConnectionProvider provider = new SmbConnectionProvider();
    provider.setHost("localhost");
    provider.setPort(445);
    provider.setShareRoot("share");
    provider.setUsername("mulesoft");
    provider.setPassword("mulesoft");
    provider.setWriteTimeout(TimeUnit.MILLISECONDS, -1);
    provider.setReadTimeout(TimeUnit.MILLISECONDS, -1);
    SmbFileSystemConnection fileSystemConnection = provider.connect();
    fileSystemConnection.getClient().write("test.txt", IOUtils.toInputStream("this is a test", Charset.defaultCharset()),
                                           FileWriteMode.OVERWRITE);
    try {
      fileSystemConnection.getClient().read("test.txt");
    } catch (MuleRuntimeException mre) {
      TransportException te = (TransportException) mre.getCause();
      assertThat(te.getMessage(), containsString("Timeout expired"));
      throw mre;
    }
  }



}
