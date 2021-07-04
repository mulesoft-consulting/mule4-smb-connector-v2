/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.test.extension.file.common.api.FileTestHarness.HELLO_PATH;
import static org.mule.test.extension.file.common.api.FileTestHarness.HELLO_WORLD;

import io.qameta.allure.Feature;
import org.junit.Test;

@Feature(AllureConstants.SmbFeature.SMB_EXTENSION)
public class SmbDeleteTestCase extends CommonSmbConnectorTestCase {


  private static final String SUB_FOLDER = "files/subfolder";
  private static final String SUB_FOLDER_FILE = "grandChild";
  private static final String SUB_FOLDER_FILE_PATH = String.format("%s/%s", SUB_FOLDER, SUB_FOLDER_FILE);
  private static final String NON_EXISTENT_FILE = "non-existent-file.txt";

  public SmbDeleteTestCase(String name, SmbTestHarness testHarness, String smbConfigFile) {
    super(name, testHarness, smbConfigFile);
  }

  @Override
  protected String getConfigFile() {
    return "smb-delete-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    testHarness.createHelloWorldFile();
    testHarness.makeDir(SUB_FOLDER);
    testHarness.write(String.format("%s/%s", SUB_FOLDER, SUB_FOLDER_FILE), HELLO_WORLD);

    assertExists(true, HELLO_PATH, SUB_FOLDER, SUB_FOLDER_FILE_PATH);
  }

  @Test
  public void deleteFile() throws Exception {
    assertThat(testHarness.fileExists(HELLO_PATH), is(true));
    doDelete(HELLO_PATH);

    assertThat(testHarness.fileExists(HELLO_PATH), is(false));
  }

  @Test
  public void deleteReadFile() throws Exception {
    assertThat(testHarness.fileExists(HELLO_PATH), is(true));
    flowRunner("delete").withVariable("delete", HELLO_PATH).run();

    assertThat(testHarness.fileExists(HELLO_PATH), is(false));
  }

  @Test
  public void deleteFolder() throws Exception {
    doDelete("files");
    assertExists(false, HELLO_PATH, SUB_FOLDER, SUB_FOLDER_FILE);
  }

  @Test
  public void deleteSubFolder() throws Exception {
    doDelete(SUB_FOLDER);
    assertExists(false, SUB_FOLDER);
  }

  @Test
  public void deleteOnBaseDirParent() throws Exception {
    final String path = ".";
    doDelete(path);
    testHarness.assertDeleted("files");
  }

  private void doDelete(String path) throws Exception {
    this.doDelete(path, true);
  }

  private void doDelete(String path, boolean failIfNotExists) throws Exception {
    flowRunner("delete")
        .withVariable("delete", path)
        .withVariable("failIfNotExists", failIfNotExists)
        .run();
  }

  private void assertExists(boolean exists, String... paths) throws Exception {
    for (String path : paths) {
      assertThat(testHarness.fileExists(path), is(exists));
    }
  }

  @Test
  public void deleteFileFailIfNotExists() throws Exception {
    testHarness.expectedError().expectErrorType("SMB", "ILLEGAL_PATH");
    testHarness.expectedError()
        .expectMessage(containsString("doesn't exist"));

    assertThat(testHarness.fileExists(NON_EXISTENT_FILE), is(false));
    doDelete(NON_EXISTENT_FILE);
  }

  @Test
  public void deleteFileSucceedIfNotExists() throws Exception {
    assertThat(testHarness.fileExists(NON_EXISTENT_FILE), is(false));
    doDelete(NON_EXISTENT_FILE, false);

    assertThat(testHarness.fileExists(NON_EXISTENT_FILE), is(false));
  }



}
