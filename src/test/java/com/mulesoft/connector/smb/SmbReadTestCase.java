/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.ThrowableCauseMatcher.hasCause;
import static org.junit.rules.ExpectedException.none;
import static org.mule.extension.file.common.api.exceptions.FileError.ILLEGAL_PATH;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;
import static com.mulesoft.connector.smb.AllureConstants.SmbFeature.SMB_EXTENSION;
import static org.mule.runtime.api.metadata.MediaType.JSON;
import static org.mule.test.extension.file.common.api.FileTestHarness.BINARY_FILE_NAME;
import static org.mule.test.extension.file.common.api.FileTestHarness.HELLO_PATH;
import static org.mule.test.extension.file.common.api.FileTestHarness.HELLO_WORLD;
import org.mule.extension.file.common.api.exceptions.DeletedFileWhileReadException;
import org.mule.extension.file.common.api.exceptions.FileBeingModifiedException;
import org.mule.extension.file.common.api.exceptions.IllegalPathException;
import com.mulesoft.connector.smb.api.SmbFileAttributes;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.event.CoreEvent;

import java.io.InputStream;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Feature(SMB_EXTENSION)
public class SmbReadTestCase extends CommonSmbConnectorTestCase {

  private static String DELETED_FILE_NAME = "deleted.txt";
  private static String DELETED_FILE_CONTENT = "non existant content";
  private static String WATCH_FILE = "watch.txt";

  public SmbReadTestCase(String name, SmbTestHarness testHarness, String smbConfigFile) {
    super(name, testHarness, smbConfigFile);
  }

  @Rule
  public ExpectedException expectedException = none();

  @Override
  protected String getConfigFile() {
    return "smb-read-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    super.doSetUp();
    testHarness.createHelloWorldFile();
  }

  @Test
  public void read() throws Exception {
    Message message = readHelloWorld().getMessage();

    assertThat(message.getPayload().getDataType().getMediaType().getPrimaryType(), is(JSON.getPrimaryType()));
    assertThat(message.getPayload().getDataType().getMediaType().getSubType(), is(JSON.getSubType()));

    assertThat(toString(message.getPayload().getValue()), is(HELLO_WORLD));
  }

  @Test
  public void readBinary() throws Exception {
    testHarness.createBinaryFile();

    Message response = readPath(BINARY_FILE_NAME, false);

    assertThat(response.getPayload().getDataType().getMediaType().getPrimaryType(), is(MediaType.BINARY.getPrimaryType()));
    assertThat(response.getPayload().getDataType().getMediaType().getSubType(), is(MediaType.BINARY.getSubType()));

    InputStream payload = (InputStream) response.getPayload().getValue();

    byte[] readContent = new byte[new Long(HELLO_WORLD.length()).intValue()];
    org.apache.commons.io.IOUtils.read(payload, readContent);
    assertThat(new String(readContent), is(HELLO_WORLD));
  }

  @Test
  public void readWithForcedMimeType() throws Exception {
    CoreEvent event = flowRunner("readWithForcedMimeType").withVariable("path", HELLO_PATH).run();
    assertThat(event.getMessage().getPayload().getDataType().getMediaType().getPrimaryType(), equalTo("test"));
    assertThat(event.getMessage().getPayload().getDataType().getMediaType().getSubType(), equalTo("test"));
  }

  @Test
  public void readUnexisting() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, ILLEGAL_PATH.getType(), IllegalPathException.class, "doesn't exist");
    readPath("files/not-there.txt");
  }

  @Test
  public void readDirectory() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, ILLEGAL_PATH.getType(), IllegalPathException.class,
                                            "since it's a directory");
    readPath("files");
  }

  @Test
  public void getProperties() throws Exception {
    SmbFileAttributes fileAttributes = (SmbFileAttributes) readHelloWorld().getMessage().getAttributes().getValue();
    testHarness.assertAttributes(HELLO_PATH, fileAttributes);
  }

  @Test
  public void readFileThatIsDeleted() throws Exception {
    // FIXME olamiral: try to reproduce this scenario and implement error handling accordingly
    expectedException.expectCause(hasCause(instanceOf(DeletedFileWhileReadException.class)));
    expectedException.expectMessage("was read but does not exist anymore.");
    testHarness.write(DELETED_FILE_NAME, DELETED_FILE_CONTENT);
    flowRunner("readFileThatIsDeleted").withVariable("path", DELETED_FILE_NAME).run().getMessage().getPayload().getValue();
  }

  @Test
  @Description("Tests the case of polling files that are still being written")
  public void readWhileStillWriting() throws Exception {
    expectedException.expectCause(hasCause(instanceOf(FileBeingModifiedException.class)));
    expectedException.expectMessage("is still being written");
    testHarness.writeByteByByteAsync(WATCH_FILE, "aaaaaaaaaa", 1000);
    flowRunner("readFileWithSizeCheck").withVariable("path", WATCH_FILE).run().getMessage().getPayload().getValue();
  }

  @Test
  @Description("Tests the case of polling files that are finishing being written")
  public void readWhileFinishWriting() throws Exception {
    testHarness.writeByteByByteAsync(WATCH_FILE, "aaa", 500);
    String result = (String) flowRunner("readFileWithSizeCheck").withVariable("path", WATCH_FILE).run().getMessage()
        .getPayload().getValue();
    assertThat(result, is("aaa"));
  }

  private Message readWithLock() throws Exception {
    Message message =
        flowRunner("readWithLock").withVariable("readPath", createUri("files/hello.json").getPath()).run().getMessage();
    return message;
  }

}
