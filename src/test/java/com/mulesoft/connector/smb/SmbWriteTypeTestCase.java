/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static com.mulesoft.connector.smb.AllureConstants.SmbFeature.SMB_EXTENSION;
import static org.mule.test.extension.file.common.api.FileTestHarness.HELLO_WORLD;
import org.mule.extension.file.common.api.FileWriteMode;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.message.OutputHandler;
import org.mule.test.runner.RunnerDelegateTo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;

import io.qameta.allure.Feature;
import org.junit.Test;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
@Feature(SMB_EXTENSION)
public class SmbWriteTypeTestCase extends CommonSmbConnectorTestCase {

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"Smb - String", new SmbTestHarness(), SMB_CONNECTION_XML, HELLO_WORLD, HELLO_WORLD},
        {"Smb - native byte", new SmbTestHarness(), SMB_CONNECTION_XML, "A".getBytes()[0], "A"},
        {"Smb - Object byte", new SmbTestHarness(), SMB_CONNECTION_XML, new Byte("A".getBytes()[0]),
            "A"},
        {"Smb - byte[]", new SmbTestHarness(), SMB_CONNECTION_XML, HELLO_WORLD.getBytes(),
            HELLO_WORLD},
        {"Smb - OutputHandler", new SmbTestHarness(), SMB_CONNECTION_XML, new TestOutputHandler(),
            HELLO_WORLD},
        {"Smb - InputStream", new SmbTestHarness(), SMB_CONNECTION_XML,
            new ByteArrayInputStream(HELLO_WORLD.getBytes()),
            HELLO_WORLD},});
  }

  private final Object content;
  private final String expected;
  private String path;

  public SmbWriteTypeTestCase(String name, SmbTestHarness testHarness, String smbConfigFile, Object content, String expected) {
    super(name, testHarness, smbConfigFile);
    this.content = content;
    this.expected = expected;
  }

  @Override
  protected String getConfigFile() {
    return "smb-write-config.xml";
  }


  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    final String folder = "test";
    testHarness.makeDir(folder);
    path = folder + "/test.txt";
  }

  @Test
  public void writeAndAssert() throws Exception {
    write(content);
    assertThat(readPathAsString(path), equalTo(expected));
  }

  private void write(Object content) throws Exception {
    doWrite(path, content, FileWriteMode.APPEND, false);
  }

  private static class TestOutputHandler implements OutputHandler {

    @Override
    public void write(CoreEvent event, OutputStream out) throws IOException {
      org.apache.commons.io.IOUtils.write(HELLO_WORLD, out);
    }
  }

}
