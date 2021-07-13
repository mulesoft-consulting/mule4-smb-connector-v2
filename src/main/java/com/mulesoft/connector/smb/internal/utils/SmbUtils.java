/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.utils;

import org.apache.commons.io.FilenameUtils;

/**
 * Utility class for normalizing SMB paths
 *
 * @since 1.0
 */
public class SmbUtils {

  private SmbUtils() {}

  public static String normalizePath(String path) {
    String result = path;
    if (result != null) {
      result = FilenameUtils.normalize(path, true);
    }

    return result;
  }

  public static String padRight(String value, int length, String padChar) {
    StringBuilder result = new StringBuilder(value);

    while (result.length() < length)
      result.append(padChar);

    return result.toString();
  }

}
