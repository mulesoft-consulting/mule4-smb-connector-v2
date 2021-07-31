/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb;

import io.qameta.allure.Feature;
import org.junit.Test;
import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.extension.file.common.api.exceptions.FileError.FILE_ALREADY_EXISTS;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;

@Feature(AllureConstants.SmbFeature.SMB_EXTENSION)
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
