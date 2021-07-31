/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.connection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileCopyModeTestCase {

  @Test
  public void testLabels() {
    assertEquals("copy", FileCopyMode.COPY.label());
    assertEquals("move", FileCopyMode.MOVE.label());
  }

}
