/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.extension.file.common.api.exceptions.FileError.FILE_ALREADY_EXISTS;
import static org.mule.extension.file.common.api.exceptions.FileError.ILLEGAL_PATH;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;
import static org.mule.extension.file.common.api.util.UriUtils.trimLastFragment;
import static com.mulesoft.connector.smb.AllureConstants.SmbFeature.SMB_EXTENSION;
import static org.mule.test.extension.file.common.api.FileTestHarness.HELLO_FILE_NAME;
import static org.mule.test.extension.file.common.api.FileTestHarness.HELLO_PATH;
import static org.mule.test.extension.file.common.api.FileTestHarness.HELLO_WORLD;
import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;
import org.mule.extension.file.common.api.exceptions.IllegalPathException;

import io.qameta.allure.Feature;
import org.junit.Test;

@Feature(SMB_EXTENSION)
public class SmbRenameTestCase extends CommonSmbConnectorTestCase {

  private static final String RENAME_TO = "renamed";

  public SmbRenameTestCase(String name, SmbTestHarness testHarness, String smbConfigFile) {
    super(name, testHarness, smbConfigFile);
  }

  @Override
  protected String getConfigFile() {
    return "smb-rename-config.xml";
  }

  @Test
  public void renameFile() throws Exception {
    testHarness.createHelloWorldFile();
    doRename(HELLO_PATH);
    assertRenamedFile();
  }

  @Test
  public void renameReadFile() throws Exception {
    testHarness.createHelloWorldFile();
    doRename("readAndRename", HELLO_PATH, RENAME_TO, false);
    assertRenamedFile();
  }

  @Test
  public void renameDirectory() throws Exception {
    testHarness.createHelloWorldFile();
    final String sourcePath = trimLastFragment(createUri(HELLO_PATH)).getPath();
    doRename(sourcePath);

    assertThat(testHarness.dirExists(sourcePath), is(false));
    assertThat(testHarness.dirExists(RENAME_TO), is(true));

    assertThat(readPathAsString(String.format("%s/%s", RENAME_TO, HELLO_FILE_NAME)), is(HELLO_WORLD));
  }

  @Test
  public void renameUnexisting() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, ILLEGAL_PATH.getType(), IllegalPathException.class, "doesn't exist");
    doRename("not-there.txt");
  }

  @Test
  public void targetPathContainsParts() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, ILLEGAL_PATH.getType(), IllegalPathException.class,
                                            "parameter of rename operation should not contain any file separator character");
    testHarness.createHelloWorldFile();
    final String sourcePath = trimLastFragment(createUri(HELLO_PATH)).getPath();
    doRename("rename", sourcePath, "path/with/parts", true);
  }

  @Test
  public void targetAlreadyExistsWithoutOverwrite() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, FILE_ALREADY_EXISTS.getType(), FileAlreadyExistsException.class,
                                            "already exists");
    final String sourceFile = "renameme.txt";
    testHarness.write(sourceFile, "rename me");
    testHarness.write(RENAME_TO, "I was here first");

    doRename(sourceFile);
  }

  @Test
  public void targetAlreadyExistsWithOverwrite() throws Exception {
    testHarness.createHelloWorldFile();
    final String parentPath = trimLastFragment(createUri(HELLO_PATH)).getPath();
    final String sourcePath = createUri(parentPath, RENAME_TO).getPath();
    testHarness.write(sourcePath, "I was here first");

    doRename(HELLO_PATH, true);
    assertRenamedFile();
  }

  private void assertRenamedFile() throws Exception {
    final String parentPath = trimLastFragment(createUri(HELLO_PATH)).getPath();
    final String targetPath = createUri(parentPath, RENAME_TO).getPath();

    assertThat(testHarness.fileExists(targetPath), is((true)));
    assertThat(testHarness.fileExists(HELLO_PATH), is((false)));
    assertThat(readPathAsString(targetPath), is(HELLO_WORLD));
  }

  private void doRename(String source) throws Exception {
    doRename("rename", source, RENAME_TO, false);
  }

  private void doRename(String source, boolean overwrite) throws Exception {
    doRename("rename", source, RENAME_TO, overwrite);
  }

  private void doRename(String flow, String source, String to, boolean overwrite) throws Exception {
    flowRunner(flow).withVariable("path", source).withVariable("to", to).withVariable("overwrite", overwrite).run();
  }
}
