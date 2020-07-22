/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.smb;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mule.extension.file.common.api.FileWriteMode.APPEND;
import static org.mule.extension.file.common.api.FileWriteMode.OVERWRITE;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;
import static org.mule.extension.file.common.api.util.UriUtils.trimLastFragment;
import static org.mule.extension.smb.SmbServer.*;

import jcifs.SmbConstants;
import org.mule.extension.AbstractSmbTestHarness;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileWriteMode;
import org.mule.extension.file.common.api.util.UriUtils;
import org.mule.extension.smb.api.LogLevel;
import org.mule.extension.smb.api.SmbFileAttributes;
import org.mule.extension.smb.internal.connection.SmbClient;
import org.mule.extension.smb.internal.connection.SmbClientFactory;
import org.mule.test.extension.file.common.api.FileTestHarness;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;

/**
 * Implementation of {@link FileTestHarness} for classic SMB connections
 *
 * @since 1.0
 */
public class SmbTestHarness extends AbstractSmbTestHarness {

  private TemporaryFolder temporaryFolder = new TemporaryFolder();
  private SmbServer smbServer;
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
    setUpServer();
    smbClient = createDefaultSmbClient();
  }

  /**
   * Disconnects the client and shuts the server down
   */
  @Override
  protected void doAfter() throws Exception {
    try {
      if (smbClient != null) {
        smbClient.disconnect();
      }

      if (smbServer != null) {
        smbServer.stop();
      }
    } finally {
      temporaryFolder.delete();
      System.clearProperty(WORKING_DIR_SYSTEM_PROPERTY);
    }
  }

  private SmbClient createDefaultSmbClient() throws Exception {
    SmbClient smbClient = new SmbClientFactory().createInstance("localhost", SmbServer.SHARE_ROOT, LogLevel.WARN);
    smbClient.login(DOMAIN, USERNAME, PASSWORD);
    return smbClient;
  }

  public void setUpServer() throws InterruptedException {
    smbServer = new SmbServer(temporaryFolder.getRoot().toPath());
    smbServer.start();
  }

  /**
   * @inheritDoc
   */
  @Override
  protected TestRule[] getChildRules() {
    return new TestRule[] {};
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
  public void createBinaryFile() throws Exception {
    smbClient.write(BINARY_FILE_NAME, new ByteArrayInputStream(HELLO_WORLD.getBytes()), OVERWRITE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void makeDir(String directoryPath) throws Exception {
    smbClient.mkdir(directoryPath);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getWorkingDirectory() throws Exception {
    // Assume the share root as the working dir
    return smbClient.getShareRoot();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(String path, String content) throws Exception {
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
  public boolean changeWorkingDirectory(String path) throws Exception {
    // Does not apply to SMB
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getFileList(String path) throws Exception {
    List<String> files = smbClient.list(path).stream().map(FileAttributes::getName).collect(toList());
    return files.toArray(new String[files.size()]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getServerPort() throws Exception {
    return SmbConstants.DEFAULT_PORT;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void assertAttributes(String path, Object attributes) throws Exception {
    SmbFileAttributes fileAttributes = (SmbFileAttributes) attributes;
    SmbFileAttributes file = smbClient.getAttributes(createUri(path));

    assertThat(fileAttributes.getName(), equalTo(file.getName()));
    assertThat(fileAttributes.getPath(), is(smbClient.resolvePath(HELLO_PATH).toString()));
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
  /*
  public enum AuthType {
    USER_PASSWORD, PUBLIC_KEY
  }
  */

  protected void writeByteByByteAsync(String path, String content, long delayBetweenCharacters) throws Exception {
    OutputStream os = smbClient.getOutputStream(path, FileWriteMode.CREATE_NEW);

    new Thread(() -> {

      try {
        byte[] bytes = content.getBytes();
        for (int i = 0; i < bytes.length; i++) {
          IOUtils.copy(new ByteArrayInputStream(new byte[] {bytes[i]}), os);
          Thread.sleep(delayBetweenCharacters);
        }
      } catch (Exception e) {
        fail("Error trying to write in file");
      }
    }).start();

  }

  public SmbServer getSmbServer() {
    return smbServer;
  }

}
