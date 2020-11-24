/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package org.mule.extension.smb;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.extension.smb.AllureConstants.SmbFeature.SMB_EXTENSION;
import static org.mule.test.extension.file.common.api.FileTestHarness.HELLO_PATH;
import static org.mule.test.extension.file.common.api.FileTestHarness.HELLO_WORLD;

import io.qameta.allure.Feature;
import org.junit.Test;

@Feature(SMB_EXTENSION)
public class SmbDeleteTestCase extends CommonSmbConnectorTestCase {

  private static final String SUB_FOLDER = "files/subfolder";
  private static final String SUB_FOLDER_FILE = "grandChild";
  private static final String SUB_FOLDER_FILE_PATH = String.format("%s/%s", SUB_FOLDER, SUB_FOLDER_FILE);

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
    flowRunner("delete").withVariable("delete", path).run();
  }

  private void assertExists(boolean exists, String... paths) throws Exception {
    for (String path : paths) {
      assertThat(testHarness.fileExists(path), is(exists));
    }
  }
}
