/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb;

import io.qameta.allure.Feature;
import org.junit.Test;
import org.mule.extension.file.common.api.exceptions.IllegalPathException;

import static com.mulesoft.connector.smb.internal.utils.SmbUtils.normalizePath;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mule.extension.file.common.api.exceptions.FileError.ILLEGAL_PATH;
import static org.mule.test.extension.file.common.api.FileTestHarness.HELLO_WORLD;

@Feature(AllureConstants.SmbFeature.SMB_EXTENSION)
public class SmbCopyTestCase extends CommonSmbConnectorTestCase {

  private static final String SOURCE_FILE_NAME = "test.txt";
  private static final String SOURCE_DIRECTORY_NAME = "source";
  private static final String TARGET_DIRECTORY = "target";
  private static final String EXISTING_CONTENT = "I was here first!";
  private static final String RENAMED = "renamed.txt";

  protected String sourcePath;

  public SmbCopyTestCase(String name, SmbTestHarness testHarness, String smbConfigFile) {
    super(name, testHarness, smbConfigFile);
  }

  @Override
  protected String getConfigFile() {
    return "smb-copy-config.xml";
  }

  private String getPath(String... path) {
    return normalizePath(String.join("/", path));
  }

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    testHarness.write(SOURCE_FILE_NAME, HELLO_WORLD);
    sourcePath = getPath(SOURCE_FILE_NAME);
  }

  @Test
  public void toExistingFolder() throws Exception {
    testHarness.makeDir(TARGET_DIRECTORY);
    final String path = getPath(TARGET_DIRECTORY);
    doExecute(path, false, false);

    assertCopy(format("%s/%s", path, SOURCE_FILE_NAME));
  }

  @Test
  public void absoluteSourcePath() throws Exception {
    final String absoluteSourcePath = getPath(SOURCE_FILE_NAME);
    testHarness.makeDir(TARGET_DIRECTORY);
    final String path = getPath(TARGET_DIRECTORY);
    doExecute(getFlowName(), absoluteSourcePath, path, false, false, null);

    assertCopy(format("%s/%s", path, SOURCE_FILE_NAME));
  }

  @Test
  public void toNonExistingFolder() throws Exception {
    testHarness.makeDir(TARGET_DIRECTORY);
    String target = format("%s/%s", TARGET_DIRECTORY, "a/b/c");
    doExecute(target, false, true);

    assertCopy(format("%s/%s", target, SOURCE_FILE_NAME));
  }

  @Test
  public void copyReadFile() throws Exception {
    testHarness.makeDir(TARGET_DIRECTORY);
    final String path = getPath(TARGET_DIRECTORY);
    doExecute("readAndDo", path, false, false, null);

    assertCopy(format("%s/%s", path, SOURCE_FILE_NAME));
  }

  @Test
  public void toNonExistingFolderWithoutCreateParent() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, ILLEGAL_PATH.getType(), IllegalPathException.class,
                                            "doesn't exist");
    testHarness.makeDir(TARGET_DIRECTORY);
    String target = format("%s/%s", TARGET_DIRECTORY, "a/b/c");
    doExecute(target, false, false);
  }

  @Test
  public void targetPathIsNotDirectory() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, ILLEGAL_PATH.getType(), IllegalPathException.class,
                                            "path exists but it's not a directory");

    final String someFile = "somefile.txt";
    testHarness.write(someFile, EXISTING_CONTENT);

    final String target = getPath(someFile);

    doExecute(target, true, false);
  }

  @Test
  public void directoryToExistingDirectory() throws Exception {
    sourcePath = buildSourceDirectory();
    final String target = "target";
    testHarness.makeDir(target);
    doExecute(target, false, false);
    assertCopy(format("%s/source/%s", target, SOURCE_FILE_NAME));
  }

  @Test
  public void directoryToNotExistingDirectory() throws Exception {
    sourcePath = buildSourceDirectory();

    String target = "a/b/c";
    doExecute(target, false, true);

    assertCopy(format("%s/source/%s", target, SOURCE_FILE_NAME));
  }

  @Test
  public void directoryAndOverwrite() throws Exception {
    sourcePath = buildSourceDirectory();

    final String target = "target";
    testHarness.makeDir(target);
    testHarness.write(target, SOURCE_FILE_NAME, EXISTING_CONTENT);

    doExecute(target, true, false);
    assertCopy(format("%s/%s/%s", target, SOURCE_DIRECTORY_NAME, SOURCE_FILE_NAME));
  }

  @Test
  public void copyAndRenameInSameDirectory() throws Exception {
    doExecute(testHarness.getWorkingDirectory(), true, false, RENAMED);
    assertCopy(RENAMED);
  }

  @Test
  public void copyAndRenameInSameDirectoryWithOverwrite() throws Exception {
    testHarness.write("", RENAMED, EXISTING_CONTENT);

    doExecute(testHarness.getWorkingDirectory(), true, false, RENAMED);
    assertCopy(RENAMED);
  }

  @Test
  public void directoryToExistingDirectoryWithRename() throws Exception {
    sourcePath = buildSourceDirectory();
    final String target = "target";
    testHarness.makeDir(target);
    doExecute(target, false, false, "renamedSource");
    assertCopy(format("%s/renamedSource/%s", target, SOURCE_FILE_NAME));
  }

  private String buildSourceDirectory() throws Exception {
    testHarness.makeDir(SOURCE_DIRECTORY_NAME);
    testHarness.write(SOURCE_DIRECTORY_NAME, SOURCE_FILE_NAME, HELLO_WORLD);

    return getPath(SOURCE_DIRECTORY_NAME);
  }

  void doExecute(String target, boolean overwrite, boolean createParentFolder) throws Exception {
    doExecute(getFlowName(), target, overwrite, createParentFolder, null);
  }

  void doExecute(String target, boolean overwrite, boolean createParentFolder, String renameTo) throws Exception {
    doExecute(getFlowName(), target, overwrite, createParentFolder, renameTo);
  }

  void doExecute(String flowName, String target, boolean overwrite, boolean createParentFolder, String renameTo)
      throws Exception {
    doExecute(flowName, sourcePath, target, overwrite, createParentFolder, renameTo);
  }

  void doExecute(String flowName, String source, String target, boolean overwrite, boolean createParentFolder,
                 String renameTo)
      throws Exception {
    flowRunner(flowName).withVariable(SOURCE_DIRECTORY_NAME, source).withVariable("target", target)
        .withVariable("overwrite", overwrite).withVariable("createParent", createParentFolder).withVariable("renameTo", renameTo)
        .run();

  }

  protected void assertCopy(String target) throws Exception {
    assertThat(readPathAsString(target), equalTo(HELLO_WORLD));
  }

  protected String getFlowName() {
    return "copy";
  }
}
