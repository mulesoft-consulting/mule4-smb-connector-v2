/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.smb;

import static java.nio.charset.Charset.availableCharsets;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mule.extension.file.common.api.FileWriteMode.APPEND;
import static org.mule.extension.file.common.api.FileWriteMode.CREATE_NEW;
import static org.mule.extension.file.common.api.FileWriteMode.OVERWRITE;
import static org.mule.extension.file.common.api.exceptions.FileError.FILE_ALREADY_EXISTS;
import static org.mule.extension.file.common.api.exceptions.FileError.ILLEGAL_PATH;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;
import static org.mule.extension.smb.AllureConstants.SmbFeature.SMB_EXTENSION;
import static org.mule.runtime.core.api.util.IOUtils.toByteArray;
import static org.mule.test.extension.file.common.api.FileTestHarness.HELLO_WORLD;

import org.mule.extension.file.common.api.FileWriteMode;
import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;
import org.mule.extension.file.common.api.exceptions.IllegalPathException;
import org.mule.runtime.core.api.event.CoreEvent;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import io.qameta.allure.Feature;
import org.junit.Ignore;
import org.junit.Test;

@Feature(SMB_EXTENSION)
public class SmbWriteTestCase extends CommonSmbConnectorTestCase {

  private static final String TEMP_DIRECTORY = "files";

  public SmbWriteTestCase(String name, SmbTestHarness testHarness, String smbConfigFile) {
    super(name, testHarness, smbConfigFile);
  }

  @Override
  protected String getConfigFile() {
    return "smb-write-config.xml";
  }

  @Test
  public void writeOnFileWithColonInName() throws Exception {
    testHarness.expectedError().expectErrorType("SMB", "CONNECTIVITY");
    testHarness.expectedError().expectMessage(containsString("The filename, directory name, or volume label syntax is incorrect."));

    final String filePath = "folder/fi:le.txt";

    doWrite(filePath, HELLO_WORLD, OVERWRITE, true);
    toString(readPath(filePath).getPayload().getValue());
  }

  @Test
  public void appendOnNotExistingFile() throws Exception {
    doWriteOnNotExistingFile(APPEND);
  }

  @Test
  public void overwriteOnNotExistingFile() throws Exception {
    doWriteOnNotExistingFile(OVERWRITE);
  }

  @Test
  public void createNewOnNotExistingFile() throws Exception {
    doWriteOnNotExistingFile(CREATE_NEW);
  }

  @Test
  public void appendOnExistingFile() throws Exception {
    String content = doWriteOnExistingFile(APPEND);
    assertThat(content, is(HELLO_WORLD + HELLO_WORLD));
  }

  @Test
  public void overwriteOnExistingFile() throws Exception {
    String content = doWriteOnExistingFile(OVERWRITE);
    assertThat(content, is(HELLO_WORLD));
  }

  @Test
  public void createNewOnExistingFile() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, FILE_ALREADY_EXISTS.getType(), FileAlreadyExistsException.class,
                                            "Use a different write mode or point to a path which doesn't exist");
    doWriteOnExistingFile(CREATE_NEW);
  }

  @Test
  public void appendOnNotExistingParentWithoutCreateFolder() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, ILLEGAL_PATH.getType(), IllegalPathException.class,
                                            "because path to it doesn't exist");
    doWriteOnNotExistingParentWithoutCreateFolder(APPEND);
  }

  @Test
  public void overwriteOnNotExistingParentWithoutCreateFolder() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, ILLEGAL_PATH.getType(), IllegalPathException.class,
                                            "because path to it doesn't exist");
    doWriteOnNotExistingParentWithoutCreateFolder(OVERWRITE);
  }

  @Test
  public void createNewOnNotExistingParentWithoutCreateFolder() throws Exception {
    testHarness.expectedError().expectError(NAMESPACE, ILLEGAL_PATH.getType(), IllegalPathException.class,
                                            "because path to it doesn't exist");
    doWriteOnNotExistingParentWithoutCreateFolder(CREATE_NEW);
  }

  //TODO: MULE-16515 ignore this test until issue is fixed.
  @Test
  @Ignore
  public void writeOnLockedFile() throws Exception {
    final String path = "file";
    testHarness.write(path, HELLO_WORLD);
    Exception exception = flowRunner("writeAlreadyLocked").withVariable("path", path).withVariable("createParent", false)
        .withVariable("mode", APPEND)
        .withVariable("encoding", null).withPayload(HELLO_WORLD).runExpectingException();
    Method methodGetErrors = exception.getCause().getClass().getMethod("getErrors");
    Object error = ((List<Object>) methodGetErrors.invoke(exception.getCause())).get(0);
    Method methodGetErrorType = error.getClass().getMethod("getErrorType");
    methodGetErrorType.setAccessible(true);
    Object fileError = methodGetErrorType.invoke(error);
    assertThat(fileError.toString(), is("FILE:FILE_LOCK"));
  }

  @Test
  public void appendNotExistingFileWithCreatedParent() throws Exception {
    doWriteNotExistingFileWithCreatedParent(APPEND);
  }

  @Test
  public void overwriteNotExistingFileWithCreatedParent() throws Exception {
    doWriteNotExistingFileWithCreatedParent(OVERWRITE);
  }

  @Test
  public void createNewNotExistingFileWithCreatedParent() throws Exception {
    doWriteNotExistingFileWithCreatedParent(CREATE_NEW);
  }

  @Test
  public void writeWithLock() throws Exception {
    testHarness.makeDir(TEMP_DIRECTORY);

    // TODO: verify if modification is valid

    // Old path assignment:
    // String path = createUri(createUri(testHarness.getWorkingDirectory(), TEMP_DIRECTORY).getPath(), "test.txt").getPath();

    // New path assignment
    String path = createUri(TEMP_DIRECTORY, "test.txt").getPath();
    doWrite("writeWithLock", path, HELLO_WORLD, CREATE_NEW, false);

    String content = toString(readPath(path).getPayload().getValue());
    assertThat(content, is(HELLO_WORLD));
  }

  @Test
  public void writeOnReadFile() throws Exception {
    final String filePath = "file";

    testHarness.write(filePath, "overwrite me!");

    CoreEvent event = flowRunner("readAndWrite").withVariable("path", filePath).run();

    assertThat(event.getMessage().getPayload().getValue(), equalTo(HELLO_WORLD));
  }

  @Test
  public void writeStaticContent() throws Exception {
    testHarness.makeDir(TEMP_DIRECTORY);

    // TODO: verify if modification is valid
    // Old path assignment:
    // String path = createUri(createUri(testHarness.getWorkingDirectory(), TEMP_DIRECTORY).getPath(), "test.txt").getPath();

    // New path assignment
    String path = createUri(TEMP_DIRECTORY, "test.txt").getPath();

    doWrite("writeStaticContent", path, "", CREATE_NEW, false);

    String content = toString(readPath(path).getPayload().getValue());
    assertThat(content, is(HELLO_WORLD));
  }

  @Test
  public void writeWithCustomEncoding() throws Exception {
    final String defaultEncoding = muleContext.getConfiguration().getDefaultEncoding();
    assertThat(defaultEncoding, is(notNullValue()));

    final String customEncoding =
        availableCharsets().keySet().stream().filter(encoding -> !encoding.equals(defaultEncoding)).findFirst().orElse(null);

    assertThat(customEncoding, is(notNullValue()));
    final String filename = "encoding.txt";

    doWrite("write", filename, HELLO_WORLD, CREATE_NEW, false, customEncoding);

    // TODO: verify if modification is valid
    // Old path assignment
    // String path = createUri(testHarness.getWorkingDirectory(), filename).getPath();
    // New path assignment
    String path = filename;
    InputStream content = (InputStream) readPath(path, false).getPayload().getValue();

    assertThat(Arrays.equals(toByteArray(content), HELLO_WORLD.getBytes(customEncoding)), is(true));
  }

  private void doWriteNotExistingFileWithCreatedParent(FileWriteMode mode) throws Exception {
    testHarness.makeDir(TEMP_DIRECTORY);
    // TODO: verify if modification is valid
    String path = createUri(createUri(testHarness.getWorkingDirectory(), TEMP_DIRECTORY).getPath(), "a/b/test.txt").getPath();
    // String path = createUri(TEMP_DIRECTORY, "a/b/test.txt").getPath();

    doWrite(path, HELLO_WORLD, mode, true);

    String content = toString(readPath(path).getPayload().getValue());
    assertThat(content, is(HELLO_WORLD));
  }


  private void doWriteOnNotExistingFile(FileWriteMode mode) throws Exception {
    testHarness.makeDir(TEMP_DIRECTORY);
    // TODO: verify if it's necessary to add the working directory
    // SmbWriteCommand considers the fileSystem.basePath, and fileSystem.basePath is populated according to
    // the workingDir connection provider property (set by default with the value "base", in test harness
    // Because of that, the working directory will be removed from the URI creation.
    // This adjusment will be applied to all test scenarios.
    // Original path assignment statement:
    //String path = createUri(createUri(testHarness.getWorkingDirectory(), TEMP_DIRECTORY).getPath(), "test.txt").getPath();

    // New path assignment statement:
    String path = createUri(TEMP_DIRECTORY, "test.txt").getPath();
    doWrite(path, HELLO_WORLD, mode, false);

    String content = toString(readPath(path));
    assertThat(content, is(HELLO_WORLD));
  }

  private void doWriteOnNotExistingParentWithoutCreateFolder(FileWriteMode mode) throws Exception {
    testHarness.makeDir(TEMP_DIRECTORY);
    String path = createUri(createUri(testHarness.getWorkingDirectory(), TEMP_DIRECTORY).getPath(), "a/b/test.txt").getPath();
    doWrite(path, HELLO_WORLD, mode, false);
  }

  private String doWriteOnExistingFile(FileWriteMode mode) throws Exception {
    final String filePath = "file";
    testHarness.write(filePath, HELLO_WORLD);

    doWrite(filePath, HELLO_WORLD, mode, false);
    return toString(readPath(filePath).getPayload().getValue());
  }

  public static InputStream getContentStream() {
    return (new InputStream() {

      String text = "Hello World!";
      char[] textArray = text.toCharArray();
      int index = -1;

      @Override
      public int read() throws IOException {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          fail();
        }
        if (index < text.length() - 1) {
          index++;
          return (int) textArray[index];
        }
        return -1;
      }
    });
  }
}
