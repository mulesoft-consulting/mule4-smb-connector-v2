/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb;

import com.mulesoft.connector.AbstractSmbTestHarness;
import com.mulesoft.connector.smb.api.SmbFileAttributes;
import com.mulesoft.connector.smb.internal.connection.SmbClientFactory;
import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import com.mulesoft.connector.smb.internal.utils.SmbUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileWriteMode;
import org.mule.extension.file.common.api.util.UriUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.extension.file.common.api.FileTestHarness;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mule.extension.file.common.api.FileWriteMode.APPEND;
import static org.mule.extension.file.common.api.FileWriteMode.OVERWRITE;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;
import static org.mule.extension.file.common.api.util.UriUtils.trimLastFragment;

/**
 * Implementation of {@link FileTestHarness} for classic SMB connections
 *
 * @since 1.0
 */
public class SmbTestHarness extends AbstractSmbTestHarness {

  private static final String SMB_PORT = "SMB_PORT";

  private final TemporaryFolder temporaryFolder = new TemporaryFolder();
  private final DynamicPort smbPort = new DynamicPort(SMB_PORT);

  private SmbClient smbClient;

  /**
   * Creates a new instance which activates the {@code smb} spring profile
   */
  // TODO: should initialize with authentication type and SMB protocol version.
  public SmbTestHarness() {
    super("smb");
  }

  /**
   * Starts a SMB server and connects a client to it
   */
  @Override
  protected void doBefore() throws Exception {
    temporaryFolder.create();
    smbClient = createDefaultSmbClient();
    List<SmbFileAttributes> files = smbClient.list("/");
    if (files != null && !files.isEmpty()) {
      for (SmbFileAttributes file : files) {
        try {
          smbClient.delete(file.getPath());
        } catch (Exception e) {
          //Does nothing
        }
      }
    }

    assertTrue(smbClient.list("/").isEmpty());
  }

  /**
   * Disconnects the client and shuts the server down
   */
  @Override
  protected void doAfter() {
    try {
      if (smbClient != null) {
        smbClient.disconnect();
      }

    } finally {
      temporaryFolder.delete();
      System.clearProperty(WORKING_DIR_SYSTEM_PROPERTY);
    }
  }

  public static SmbClient createDefaultSmbClient() throws Exception {
    SmbClient smbClient =
        new SmbClientFactory().createInstance(SmbServer.HOSTNAME, SmbServer.PORT, SmbServer.SHARE_ROOT, true);
    smbClient.setPassword(SmbServer.PASSWORD);
    smbClient.login(SmbServer.DOMAIN, SmbServer.USERNAME);
    return smbClient;
  }

  /**
   * @return {@link #smbPort}
   */
  @Override
  protected TestRule[] getChildRules() {
    return new TestRule[] {smbPort};
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createHelloWorldFile() throws Exception {
    final String dir = "files";
    makeDir(dir);
    write(dir, HELLO_FILE_NAME, HELLO_WORLD);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void createBinaryFile() {
    smbClient.write(BINARY_FILE_NAME, new ByteArrayInputStream(HELLO_WORLD.getBytes()), OVERWRITE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void makeDir(String directoryPath) {
    smbClient.mkdir(directoryPath);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getWorkingDirectory() {
    // Assume the share root as the working dir
    return "/";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(String path, String content) {
    smbClient.write(path, new ByteArrayInputStream(content.getBytes()), APPEND);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean dirExists(String path) throws Exception {
    FileAttributes attributes = smbClient.getAttributes(createUri(path));
    return attributes != null && attributes.isDirectory();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean fileExists(String path) throws Exception {
    return smbClient.getAttributes(createUri(path)) != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean changeWorkingDirectory(String path) {
    // Does not apply to SMB
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getFileList(String path) {
    return smbClient.list(path).stream().map(FileAttributes::getName).toArray(String[]::new);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getServerPort() {
    return SmbServer.PORT;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void assertAttributes(String path, Object attributes) throws Exception {
    SmbFileAttributes fileAttributes = (SmbFileAttributes) attributes;
    SmbFileAttributes file = smbClient.getAttributes(createUri(path));

    assertThat(fileAttributes.getName(), equalTo(file.getName()));
    assertThat(fileAttributes.getPath(), is(this.resolvePath(HELLO_PATH).getPath()));
    assertThat(fileAttributes.getSize(), is(file.getSize()));
    assertThat(fileAttributes.getTimestamp(), equalTo(file.getTimestamp()));
    assertThat(fileAttributes.isDirectory(), is(false));
    assertThat(fileAttributes.isSymbolicLink(), is(false));
    assertThat(fileAttributes.isRegularFile(), is(true));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void assertDeleted(String path) throws Exception {
    URI directoryUri = createUri(path);

    String lastFragment2 = FilenameUtils.getName(directoryUri.getPath());
    if (".".equals(lastFragment2)) {
      directoryUri = trimLastFragment(directoryUri);
    }

    assertThat(dirExists(directoryUri.getPath()), is(false));
  }

  // TODO: review possible authentication type to SMB Server (NTLM, Kerberos, and so on)
  // TODO: implement validation according to different SMB protocol versions


  protected void writeByteByByteAsync(String path, String content, long delayBetweenCharacters) {
    OutputStream os = smbClient.getOutputStream(path, FileWriteMode.CREATE_NEW);

    new Thread(() -> {

      try {
        byte[] bytes = content.getBytes();
        for (byte aByte : bytes) {
          IOUtils.copy(new ByteArrayInputStream(new byte[] {aByte}), os);
          os.flush();
          Thread.sleep(delayBetweenCharacters);
        }
      } catch (Exception e) {
        fail("Error trying to write in file");
      }
    }).start();

  }

  private URI resolvePath(String filePath) {
    URI result = null;

    if (filePath != null) {
      String actualFilePath = filePath;
      if (!actualFilePath.startsWith("/")) {
        actualFilePath = "/" + actualFilePath;
      }
      result = UriUtils.createUri(SmbUtils.normalizePath(actualFilePath));
    }
    return result;
  }


}
