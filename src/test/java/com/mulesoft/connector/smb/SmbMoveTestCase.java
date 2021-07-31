/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import io.qameta.allure.Feature;

@Feature(AllureConstants.SmbFeature.SMB_EXTENSION)
public class SmbMoveTestCase extends SmbCopyTestCase {

  public SmbMoveTestCase(String name, SmbTestHarness testHarness, String smbConfigFile) {
    super(name, testHarness, smbConfigFile);
  }

  @Override
  protected String getConfigFile() {
    return "smb-move-config.xml";
  }

  @Override
  protected String getFlowName() {
    return "move";
  }

  @Override
  protected void assertCopy(String target) throws Exception {
    super.assertCopy(target);
    assertThat(testHarness.fileExists(sourcePath), is(false));
  }
}
