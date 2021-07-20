/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal;

import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import com.mulesoft.connector.smb.internal.utils.SmbUtils;
import org.apache.commons.io.IOUtils;
import org.mule.extension.file.common.api.FileWriteMode;
import org.mule.extension.file.common.api.exceptions.FileLockedException;
import org.slf4j.Logger;

import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.slf4j.LoggerFactory.getLogger;

public class LoggerMessageProcessor {

  private static final Logger logger = getLogger(LoggerMessageProcessor.class);

  private LoggerMessageProcessor() {
    super();
  }

  public static boolean isEnabled(LogLevel logLevel) {
    return logLevel.isEnabled(logger);
  }

  public static void writeToLocalLog(LogLevel logLevel, String message) {
    logLevel.log(logger, message);
  }

  public static void writeToLog(SmbFileSystemConnection fileSystem, String path, LogLevel logLevel, String message) {
    boolean done;
    do {
      done = doWriteLog(fileSystem, path, logLevel, message);
    } while (!done);
  }

  private static boolean doWriteLog(SmbFileSystemConnection fileSystem, String path, LogLevel logLevel, String message) {
    boolean result = false;
    try {
      fileSystem.write(path, IOUtils.toInputStream(
                                                   SmbUtils.padRight(logLevel.name(), 6, " ")
                                                       + ZonedDateTime.now()
                                                           .format(DateTimeFormatter
                                                               .ofPattern("yyyy-MM-dd HH:mm:ss,SSSZ: "))
                                                       + message
                                                       + "\n",
                                                   Charset.defaultCharset()),
                       FileWriteMode.APPEND, true, true);
      result = true;
    } catch (FileLockedException fle) {
      logger.debug("Log file is locked ({}): ", fle.getMessage(), fle);
    }
    return result;
  }

}
