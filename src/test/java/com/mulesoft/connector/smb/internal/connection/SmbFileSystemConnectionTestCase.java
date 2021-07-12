/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection;

import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.msfscc.fileinformation.FileStandardInformation;
import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.api.SmbFileAttributes;
import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import com.mulesoft.connector.smb.internal.error.exception.SmbConnectionException;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.extension.file.common.api.lock.UriLock;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.core.internal.lock.MuleLockFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SmbFileSystemConnectionTestCase {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void createSmbFileSystemConnectionWithNullClient() {
    expectedException.expect(NullPointerException.class);
    SmbFileSystemConnection sfc = new SmbFileSystemConnection(null, null);
    sfc.validateConnection();
  }

  @Test
  public void validateConnectionWhenClientDisconnected() {
    SmbClient client = mock(SmbClient.class);
    when(client.isConnected()).thenReturn(false);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    ConnectionValidationResult validationResult = sfc.validateConnection();
    assertFalse(validationResult.isValid());
    assertThat(validationResult.getException(), instanceOf(SmbConnectionException.class));
    assertThat(validationResult.getMessage(), containsString("Connection is stale"));
  }

  @Test
  public void validateConnectionWhenClientConnected() {
    SmbClient client = mock(SmbClient.class);
    when(client.isConnected()).thenReturn(true);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    ConnectionValidationResult validationResult = sfc.validateConnection();
    assertTrue(validationResult.isValid());
    assertNull(validationResult.getException());
  }

  @Test
  public void testGetters() {
    SmbClient client = mock(SmbClient.class);
    when(client.isConnected()).thenReturn(true);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    assertEquals(client, sfc.getClient());
    assertNotNull(sfc.getReadCommand());
    assertNotNull(sfc.getCopyCommand());
    assertNotNull(sfc.getDeleteCommand());
    assertNotNull(sfc.getCreateDirectoryCommand());
    assertNotNull(sfc.getListCommand());
    assertNotNull(sfc.getMoveCommand());
    assertNotNull(sfc.getRenameCommand());
    assertNotNull(sfc.getWriteCommand());
    assertEquals("", sfc.getBasePath());
  }

  @Test
  public void retrieveFileContentWithNullFileAttributes() {
    expectedException.expect(NullPointerException.class);
    SmbClient client = mock(SmbClient.class);
    when(client.isConnected()).thenReturn(true);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    sfc.retrieveFileContent(null);
  }

  @Test
  public void retrieveFileContent() throws IOException {
    SmbClient client = mock(SmbClient.class);
    when(client.read(any())).thenReturn(IOUtils.toInputStream("this is a test", Charset.defaultCharset()));

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    SmbFileAttributes attributes = mock(SmbFileAttributes.class);
    assertEquals("this is a test", IOUtils.toString(sfc.retrieveFileContent(attributes), Charset.defaultCharset()));
  }

  @Test
  public void readFileAttributes() throws Exception {
    SmbClient client = mock(SmbClient.class);
    FileAllInformation fileInfo = mock(FileAllInformation.class);
    FileBasicInformation basicInfo = new FileBasicInformation(
                                                              new FileTime(System.currentTimeMillis()),
                                                              new FileTime(System.currentTimeMillis()),
                                                              new FileTime(System.currentTimeMillis()),
                                                              new FileTime(System.currentTimeMillis()), 0);
    when(fileInfo.getBasicInformation()).thenReturn(basicInfo);

    when(fileInfo.getNameInformation()).thenReturn("teste.txt");

    FileStandardInformation standardInfo = mock(FileStandardInformation.class);
    when(standardInfo.getEndOfFile()).thenReturn(1024L);
    when(standardInfo.getAllocationSize()).thenReturn(2048L);
    when(standardInfo.getNumberOfLinks()).thenReturn(0L);

    when(fileInfo.getStandardInformation()).thenReturn(standardInfo);

    SmbFileAttributes expectedAttributes = new SmbFileAttributes(new URI("test.txt"), fileInfo);
    when(client.getAttributes(new URI("/test.txt"))).thenReturn(expectedAttributes);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    assertEquals(expectedAttributes, sfc.readFileAttributes("test.txt"));
  }

  @Test
  public void lockWithNullClient() {
    expectedException.expect(NullPointerException.class);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(null, null);
    sfc.lock((URI) null);
  }

  @Test
  public void createLockWithNullLockFactory() {
    expectedException.expect(NullPointerException.class);
    SmbClient client = mock(SmbClient.class);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    sfc.lock((URI) null);
  }

  @Test
  public void createLockWithNullUri() {
    expectedException.expect(NullPointerException.class);

    SmbClient client = mock(SmbClient.class);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, new MuleLockFactory());
    sfc.lock((URI) null);
  }

  @Test
  public void createLock() throws URISyntaxException {
    SmbClient client = mock(SmbClient.class);
    URI uri = new URI("/test.txt");
    when(client.resolve(uri)).thenReturn(uri);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, new MuleLockFactory());
    UriLock lock = sfc.createLock(uri);

    assertNotNull(lock);
    assertEquals(lock.getUri(), uri);
  }

  @Test
  public void disconnectWithNullClient() {
    expectedException.expect(NullPointerException.class);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(null, null);
    sfc.disconnect();
  }

  @Test
  public void disconnect() {
    SmbClient client = mock(SmbClient.class);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    sfc.disconnect();
    assertFalse(sfc.isConnected());
  }

  @Test
  public void isLogLevelEnabledWithNullClient() {
    expectedException.expect(NullPointerException.class);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(null, null);
    sfc.isLogLevelEnabled(null);
  }

  @Test
  public void isLogLevelEnabledWithNullConfiguredAndComparedLogLevels() {
    SmbClient client = new SmbClient(null, 0, null, false, null);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    assertFalse(sfc.isLogLevelEnabled(null));
  }

  @Test
  public void isLogLevelEnabledWithNullConfiguredLogLevel() {
    SmbClient client = new SmbClient(null, 0, null, false, null);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    assertFalse(sfc.isLogLevelEnabled(LogLevel.INFO));
  }

  @Test
  public void isLogLevelEnabledWithNullComparedLogLevel() {
    SmbClient client = new SmbClient(null, 0, null, false, LogLevel.INFO);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    assertFalse(sfc.isLogLevelEnabled(null));
  }

  @Test
  public void testLogLevelEnabled() {
    SmbClient client = new SmbClient(null, 0, null, false, LogLevel.INFO);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    assertTrue(sfc.isLogLevelEnabled(LogLevel.WARN));
  }

  @Test
  public void testLogLevelDisabled() {
    SmbClient client = new SmbClient(null, 0, null, false, LogLevel.WARN);

    SmbFileSystemConnection sfc = new SmbFileSystemConnection(client, null);
    assertFalse(sfc.isLogLevelEnabled(LogLevel.INFO));
  }

}
