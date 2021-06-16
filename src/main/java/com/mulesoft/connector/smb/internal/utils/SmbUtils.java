/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.utils;

import org.apache.commons.io.FilenameUtils;
import org.mule.extension.file.common.api.util.UriUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for normalizing SMB paths
 *
 * @since 1.0
 */
public class SmbUtils {

  private static String SEPARATOR = "/";

  private SmbUtils() {}

  // FIXME: check if the methods declared in this class are not defined somewhere else

  public static URI resolve(String host, int port, String shareRoot, URI uri) throws Exception {
    URI result = uri;

    if (!isResolved(uri)) {
      String path = addSeparator(host + ":" + port);

      if (shareRoot != null) {
        path += addSeparator(shareRoot);
      }

      if (uri != null) {
        path += UriUtils.normalizeUri(uri);
      }

      result = new URI("smb://" + path.replaceAll("/+", "/"));

    }
    return result;
  }

  private static boolean isResolved(URI uri) {
    return uri != null && uri.toString().startsWith("smb://");
  }

  public static URI createUri(String path) {
    URI result = null;
    if (path != null) {
      result = URI.create(path);
    }
    return result;
  }

  public static String normalizePath(String path) {
    String result = path;
    if (result != null && !result.startsWith("smb://")) {
      result = FilenameUtils.normalize(path, true);
    }

    return result;
  }


  /*
  private static String getPath(String basePath, String name) {
      String result = name;
  
      if (basePath != null && !basePath.isEmpty()) {
          result = addSeparator(basePath) + name.replaceFirst("^/", "");
      }
  
      return result;
  }
  
   */

  private static String getDecodedPath(URI uri) throws Exception {
    String result = null;

    if (uri != null) {
      result = URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8.name());
    }
    return result;
  }

  // TODO olamiral: delete or adjust
  /*
  private static URI createUri(String basePath, String path) {
      URI result = null;
  
      if (baseUri != null && baseUri.toString().startsWith("smb://")) {
          result = URI.create(addSeparator(baseUri.toString()) + (path == null ? "" : path));
      } else {
          result = UriUtils.createUri(baseUri != null ? baseUri.getPath() : "", path);
      }
  
      return result;
  }
  */

  /**
   * Adds a separator at the end of the given path. If the path already ends with the separator, then
   * this method does nothing.
   */
  private static String addSeparator(String path) {
    return (path.endsWith(SEPARATOR) || path.length() == 1) ? path : path + SEPARATOR;
  }

  public static String padRight(String value, int length, String padChar) {
    String result = value;

    if (result != null) {
      while (result.length() < length) {
        result = result + padChar;
      }
    }

    return result;
  }

  public static String urlEncodePathFragments(String url) {
    String result = null;

    if (url != null) {
      try {
        URI uri = new URI(url);
        String[] fragments = uri.getPath().replaceAll("(?<!:)\\/\\/", "/").split("/");
        for (String fragment : fragments) {
          result = (result == null ? "" : result + "/") + URLEncoder.encode(fragment, StandardCharsets.UTF_8.name());
        }
        if (uri.getScheme() != null) {
          result = uri.getScheme()
              + "://" + uri.getHost()
              + (uri.getPort() > -1 ? ":" + uri.getPort() : "")
              + result;
        }
      } catch (Exception e) {
        throw new RuntimeException("Could not URL encode path fragments for URL " + url, e);
      }

      if (result != null) {
        if (url.endsWith("/")) {
          result = result + "/";
        }
      } else {
        result = url;
      }
    }

    return result;
  }
}
