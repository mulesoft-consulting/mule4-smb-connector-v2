/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.smb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.extension.smb.AllureConstants.SmbFeature.SMB_EXTENSION;

import io.qameta.allure.Feature;

@Feature(SMB_EXTENSION)
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
