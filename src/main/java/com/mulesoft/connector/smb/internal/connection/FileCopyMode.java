/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
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
