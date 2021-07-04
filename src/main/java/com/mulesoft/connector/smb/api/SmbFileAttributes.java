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
  private LocalDateTime createTime;

  @Parameter
  private LocalDateTime lastModified;

  @Parameter
  private LocalDateTime lastAccess;

  @Parameter
  private String absolutePath;

  /**
   * Creates a new instance
   *
   * @param file the file from which the attributes will be read
   * @throws Exception
   */
  public SmbFileAttributes(URI uri, FileAllInformation file) throws Exception {
    super(uri);
    this.populate(file);
  }

  protected void populate(FileAllInformation file) throws Exception {
    this.setExists(file != null);
    if (file != null) {
      this.setAbsolutePath(file.getNameInformation().replaceAll("\\\\", "/"));
      this.setSize(file.getStandardInformation().getEndOfFile());
      this.setDirectory(file.getStandardInformation().isDirectory());
      this.setRegularFile(!this.isDirectory());
      this.setCreateTime(this.localDateTimeFromEpoch(file.getBasicInformation().getCreationTime().toEpochMillis()));
      this.setLastModified(localDateTimeFromEpoch(file.getBasicInformation().getLastWriteTime().toEpochMillis()));
      this.setLastAccess(localDateTimeFromEpoch(file.getBasicInformation().getLastAccessTime().toEpochMillis()));
      this.setTimestamp(localDateTimeFromEpoch(file.getBasicInformation().getLastWriteTime().toEpochMillis()));
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

  public LocalDateTime getCreateTime() {
    return createTime;
  }

  public LocalDateTime getLastModified() {
    return lastModified;
  }

  public LocalDateTime getLastAccess() {
    return lastAccess;
  }

  public boolean exists() {
    return exists;
  }

  @Override
  public String getPath() {
    return this.absolutePath;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SmbFileAttributes that = (SmbFileAttributes) o;
    return size == that.size && directory == that.directory && regularFile == that.regularFile && exists == that.exists
        && Objects.equals(timestamp, that.timestamp) && Objects.equals(createTime, that.createTime)
        && Objects.equals(lastModified, that.lastModified) && Objects.equals(lastAccess, that.lastAccess)
        && Objects.equals(absolutePath, that.absolutePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(size, directory, regularFile, exists, timestamp, createTime, lastModified, lastAccess, absolutePath);
  }

  protected void setSize(long size) {
    this.size = size;
  }

  protected void setDirectory(boolean directory) {
    this.directory = directory;
  }

  protected void setRegularFile(boolean regularFile) {
    this.regularFile = regularFile;
  }

  protected void setExists(boolean exists) {
    this.exists = exists;
  }

  protected void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  protected void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  protected void setLastModified(LocalDateTime lastModified) {
    this.lastModified = lastModified;
  }

  protected void setLastAccess(LocalDateTime lastAccess) {
    this.lastAccess = lastAccess;
  }

  protected void setAbsolutePath(String absolutePath) {
    this.absolutePath = absolutePath;
  }


}
