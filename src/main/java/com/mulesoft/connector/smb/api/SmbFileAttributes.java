/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
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
  private boolean directory;

  @Parameter
  private String absolutePath;

  /**
   * Creates a new instance
   *
   * @param file the file from which the attributes will be read
   */
  public SmbFileAttributes(URI uri, FileAllInformation file) {
    super(uri);
    this.populate(file);
  }

  protected void populate(FileAllInformation file) {
    this.absolutePath = file.getNameInformation().replace("\\", "/");
    this.size = file.getStandardInformation().getEndOfFile();
    this.directory = file.getStandardInformation().isDirectory();
    this.timestamp = localDateTimeFromEpoch(file.getBasicInformation().getLastWriteTime().toEpochMillis());
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
    return !this.directory;
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

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SmbFileAttributes that = (SmbFileAttributes) o;
    return size == that.size && directory == that.directory
        && Objects.equals(timestamp, that.timestamp) && Objects.equals(absolutePath, that.absolutePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(size, directory, timestamp, absolutePath);
  }


}
