/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection;

import com.mulesoft.connector.smb.api.LogLevel;
import com.mulesoft.connector.smb.api.SmbFileAttributes;
import org.mule.extension.file.common.api.FileWriteMode;
import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.runtime.api.connection.ConnectionException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

/**
 * SmbClient
 *
 * @since 1.0
 */
public interface SmbClient {

  void login(String domain, String username, String password) throws Exception;

  boolean isConnected();

  void disconnect();

  /* OPERATIONS */
  void mkdir(URI uri);

  void mkdir(String dirPath);

  List<SmbFileAttributes> list(String directory);

  void write(String target, InputStream inputStream, FileWriteMode mode);

  OutputStream getOutputStream(String target, FileWriteMode mode);

  InputStream read(String filePath);

  void rename(String sourcePath, String newName, boolean overwrite);

  public void copy(String sourcePath, String targetPath);

  public void move(String sourcePath, String targetPath);

  public void delete(String path);

  /* HELPER Methods */
  SmbFileAttributes getAttributes(URI uri) throws Exception;

  URI resolve(URI uri);

  URI resolvePath(String filePath);

  String getShareRootURL();

  /* Getters and Setters */
  String getShareRoot() throws Exception;

  boolean pathIsShareRoot(String path);

  boolean isLogLevelEnabled(LogLevel logLevel);

  FileError getFileErrorFor(Exception e);
}
