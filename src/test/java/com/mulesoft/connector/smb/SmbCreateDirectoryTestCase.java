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
import static org.junit.Assume.assumeTrue;
import static org.mule.extension.file.common.api.exceptions.FileError.FILE_ALREADY_EXISTS;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;
import static com.mulesoft.connector.smb.AllureConstants.SmbFeature.SMB_EXTENSION;

import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;

import io.qameta.allure.Feature;
import org.junit.Test;

@Feature(SMB_EXTENSION)
public class SmbCreateDirectoryTestCase extends CommonSmbConnectorTestCase {

  private static final String DIRECTORY = "validDirectory";
  private static final String ROOT_CHILD_DIRECTORY = "rootChildDirectory";

  public SmbCreateDirectoryTestCase(String name, SmbTestHarness testHarness, String smbConfigFile) {
    super(name, testHarness, smbConfigFile);
  }

  @Override
  protected String getConfigFile() {
    return "smb-create-directory-config.xml";
  }

  @Test
  public void createDirectory() throws Exception {
    doCreateDirectory(DIRECTORY);
    assertThat(testHarness.dirExists(DIRECTORY), is(true));
  }

  @Test
  public void createExistingDirectory() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, FILE_ALREADY_EXISTS.getType(), FileAlreadyExistsException.class,
                                            "already exists");
    final String directory = "washerefirst";
    testHarness.makeDir(directory);
    doCreateDirectory(directory);
  }

  @Test
  public void createDirectoryWithComplexPath() throws Exception {
    String complexPath = createUri(testHarness.getWorkingDirectory(), DIRECTORY).getPath();
    doCreateDirectory(complexPath);

    assertThat(testHarness.dirExists(complexPath), is(true));
  }

  @Test
  public void createDirectoryFromRoot() throws Exception {
    String rootChildDirectoryPath = createUri(ROOT_CHILD_DIRECTORY).getPath();
    doCreateDirectory(rootChildDirectoryPath);
    assertThat(testHarness.dirExists(rootChildDirectoryPath), is(true));
  }

  @Test
  public void createRootDirectory() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, FILE_ALREADY_EXISTS.getType(), FileAlreadyExistsException.class,
                                            "already exists");
    doCreateDirectory("/");
  }

  @Test
  public void createRootCurrentDirectory() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, FILE_ALREADY_EXISTS.getType(), FileAlreadyExistsException.class,
                                            "already exists");
    doCreateDirectory("/.");
  }

  @Test
  public void createRootParentDirectory() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, FILE_ALREADY_EXISTS.getType(), FileAlreadyExistsException.class,
                                            "already exists");
    doCreateDirectory("/..");
  }

  @Test
  public void createCurrentDirectory() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, FILE_ALREADY_EXISTS.getType(), FileAlreadyExistsException.class,
                                            "already exists");
    doCreateDirectory(".");
  }

  @Test
  public void createParentDirectory() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, FILE_ALREADY_EXISTS.getType(), FileAlreadyExistsException.class,
                                            "already exists");
    doCreateDirectory("..");
  }

  @Test
  public void createParentParentDirectory() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, FILE_ALREADY_EXISTS.getType(), FileAlreadyExistsException.class,
                                            "already exists");
    doCreateDirectory("../..");
  }

  @Test
  public void createDirectoryTwice() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, FILE_ALREADY_EXISTS.getType(), FileAlreadyExistsException.class,
                                            "already exists");
    doCreateDirectory("zarasa/..");
  }

  @Test
  public void createCurrentDirectoryWithNonExistingParent() throws Exception {
    doCreateDirectory("zarasa/.");
    assertThat(testHarness.dirExists("zarasa"), is(true));
  }

  @Test
  public void createDirectoryEndingInSlash() throws Exception {
    doCreateDirectory("zarasa/");
    assertThat(testHarness.dirExists("zarasa"), is(true));
  }

  @Test
  public void createBlankDirectory() throws Exception {
    testHarness.expectedError().expectErrorType("SMB", "ILLEGAL_PATH");
    testHarness.expectedError().expectMessage(containsString("directory path cannot be null nor blank"));
    doCreateDirectory("");
  }

  @Test
  public void createDirectoryWithSpace() throws Exception {
    testHarness.expectedError().expectErrorType("SMB", "ILLEGAL_PATH");
    testHarness.expectedError().expectMessage(containsString("directory path cannot be null nor blank"));
    doCreateDirectory(" ");
  }

  @Test
  public void createComplexDirectoryWithSpace() throws Exception {
    // TODO: confirm if this behavior is expected!
    // The test createDirectoryWithSpace verifies that directory creation will fail if directory is blank
    // In this case, all paths will resolve to "zarasa/valid" and "zaraza/"
    // If it is expected that a directory with an blank name exists, this test scenario should be revised.
    doCreateDirectory("zarasa/ /valid");
    assertThat(testHarness.dirExists("zarasa/ "), is(true));
    assertThat(testHarness.dirExists("zarasa/ /valid"), is(true));
  }

  @Test
  public void createDirectoryWithSpaceAndSlash() throws Exception {
    testHarness.expectedError().expectMessage(containsString("directory path cannot be null nor blank"));

    doCreateDirectory(" /");
    assertThat(testHarness.dirExists(" "), is(true));
  }

  @Test
  public void createDirectoryWithSpecialCharacter() throws Exception {
    doCreateDirectory("@");
    assertThat(testHarness.dirExists("@"), is(true));
  }

  @Test
  public void createCurrentDirectoryAndChildDirectoryIgnoresDot() throws Exception {
    doCreateDirectory("./valid");
    assertThat(testHarness.dirExists("valid"), is(true));
  }

  @Test
  public void createParentDirectoryAndChildDirectory() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, FILE_ALREADY_EXISTS.getType(), FileAlreadyExistsException.class,
                                            "already exists");
    doCreateDirectory("../valid");
  }

  @Test
  public void createDirectoryStartingWithSlashCreatesAbsoluteDirectory() throws Exception {
    doCreateDirectory("/secondBase/child");
    assertThat(testHarness.dirExists("/secondBase/child"), is(true));
    assertThat(testHarness.dirExists("/base/secondBase/child"), is(false));
  }

  @Test
  public void createRelativeDirectoryResolvesCorrectly() throws Exception {
    testHarness.makeDir("child");
    doCreateDirectory("child/secondChild");
    assertThat(testHarness.dirExists("child/secondChild"), is(true));
    assertThat(testHarness.dirExists("child/child/secondChild"), is(false));
    assertThat(testHarness.dirExists("child/child"), is(false));
  }

  @Test
  public void createDirectoryWithColon() throws Exception {
    testHarness.expectedError().expectErrorType("SMB", "CONNECTIVITY");
    testHarness.expectedError()
        .expectMessage(containsString("The filename, directory name, or volume label syntax is incorrect."));

    final String path = "pathWith:Colon";
    doCreateDirectory(path);
    assertThat(testHarness.dirExists("/base/pathWith:Colon"), is(false));
  }

  @Test
  public void createDirectoryWithGreaterThan() throws Exception {
    testHarness.expectedError().expectErrorType("SMB", "CONNECTIVITY");
    testHarness.expectedError()
        .expectMessage(containsString("The filename, directory name, or volume label syntax is incorrect."));

    final String path = "pathWith>";
    doCreateDirectory(path);
  }

  private void doCreateDirectory(String directory) throws Exception {
    flowRunner("createDirectory").withVariable("directory", directory).run();
  }
}
