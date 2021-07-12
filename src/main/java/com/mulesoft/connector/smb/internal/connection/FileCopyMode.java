/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection;

public enum FileCopyMode {
  COPY("copy"), MOVE("move");

  private final String label;

  FileCopyMode(String label) {
    this.label = label;
  }

  public String label() {
    return this.label;
  }
}
