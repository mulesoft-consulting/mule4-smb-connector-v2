/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.client.impl.smbj;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.mulesoft.connector.smb.api.SmbFileAttributes;

import java.net.URI;

public class SmbjFileAttributes extends SmbFileAttributes<FileAllInformation> {

  public SmbjFileAttributes(URI uri, FileAllInformation fileInformation) throws Exception {
    super(uri, fileInformation);
  }

  @Override
  protected void populate(FileAllInformation file) {
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

}
