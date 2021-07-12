/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
