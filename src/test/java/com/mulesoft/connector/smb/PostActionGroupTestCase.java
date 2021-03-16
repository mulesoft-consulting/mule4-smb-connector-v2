/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb;

import static com.mulesoft.connector.smb.AllureConstants.SmbFeature.SMB_EXTENSION;
import com.mulesoft.connector.smb.internal.source.PostActionGroup;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.Test;

@SmallTest
@Feature(SMB_EXTENSION)
public class PostActionGroupTestCase extends AbstractMuleTestCase {

  @Test
  @Description("tests all the valid states of post action parameters")
  public void validAction() {
    new PostActionGroup(true, null, null, true).validateSelf();
    new PostActionGroup(true, null, null, false).validateSelf();
    new PostActionGroup(false, "someDir", null, false).validateSelf();
    new PostActionGroup(false, "someDir", "thisone.txt", false).validateSelf();
    new PostActionGroup(false, null, "thisone.txt", false).validateSelf();
  }

  @Test(expected = IllegalArgumentException.class)
  @Description("verifies that autoDelete and moveToDirectory cannot be set at the same time")
  public void deleteAndMove() {
    new PostActionGroup(true, "someDir", null, true).validateSelf();
  }

  @Test(expected = IllegalArgumentException.class)
  @Description("verifies that autoDelete and renameTo cannot be set at the same time")
  public void deleteAndRename() {
    new PostActionGroup(true, null, "thisone.txt", true).validateSelf();
  }
}
