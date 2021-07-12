/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.api;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.mulesoft.connector.smb.internal.codecoverage.ExcludeFromGeneratedCoverageReport;
import org.mule.extension.file.common.api.AbstractFileAttributes;
import org.mule.runtime.extension.api.annotation.param.Parameter;


/**
 * Metadata about a file in a SMBs server
 *
 * @since 1.0
 */
public class SmbFileAttributes extends AbstractFileAttributes {

  /**
   *
   */
  private static final long serialVersionUID = -2357110369914630644L;

  @Parameter
  private LocalDateTime timestamp;

  @Parameter
  private long size;

  @Parameter
  private boolean regularFile;

  @Parameter
  private boolean directory;

  @Parameter
  private boolean exists;

  @Parameter
  private String absolutePath;

  /**
   * Creates a new instance
   *
   * @param file the file from which the attributes will be read
   * @throws Exception if any exception occurs
   */
  public SmbFileAttributes(URI uri, FileAllInformation file) throws Exception {
    super(uri);
    this.populate(file);
  }

  protected void populate(FileAllInformation file) {
    this.setExists(file != null);
    if (file != null) {
      this.absolutePath = file.getNameInformation().replaceAll("\\\\", "/");
      this.size = file.getStandardInformation().getEndOfFile();
      this.directory = file.getStandardInformation().isDirectory();
      this.regularFile = !this.isDirectory();
      this.timestamp = localDateTimeFromEpoch(file.getBasicInformation().getLastWriteTime().toEpochMillis());
    }
  }

  protected LocalDateTime localDateTimeFromEpoch(long epochMilli) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
  }

  @Override
  public long getSize() {
    return this.size;
  }

  @Override
  public boolean isDirectory() {
    return this.directory;
  }

  @Override
  public boolean isRegularFile() {
    return this.regularFile;
  }

  @Override
  public boolean isSymbolicLink() {
    return false;
  }

  /**
   * @return The last time the file was modified
   */
  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  @Override
  public String getPath() {
    return this.absolutePath;
  }

  protected void setExists(boolean exists) {
    this.exists = exists;
  }

  @Override
  @ExcludeFromGeneratedCoverageReport("Not called in functional tests")
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SmbFileAttributes that = (SmbFileAttributes) o;
    return size == that.size && directory == that.directory && regularFile == that.regularFile && exists == that.exists
        && Objects.equals(timestamp, that.timestamp) && Objects.equals(absolutePath, that.absolutePath);
  }

  @Override
  @ExcludeFromGeneratedCoverageReport("Not called in functional tests")
  public int hashCode() {
    return Objects.hash(size, directory, regularFile, exists, timestamp, absolutePath);
  }


}
