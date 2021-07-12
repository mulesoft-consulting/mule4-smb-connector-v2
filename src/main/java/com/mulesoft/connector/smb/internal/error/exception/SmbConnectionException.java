/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.error.exception;

import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * {@link ConnectionException} implementation to declare connectivity errors in
 * the SmbConnector
 *
 * @since 1.0
 */
public class SmbConnectionException extends ConnectionException {

  public SmbConnectionException(String message, FileError errors) {
    super(message, new ModuleException(message, errors));
  }

  public SmbConnectionException(String message, Throwable throwable, FileError fileError) {
    super(message, new ModuleException(fileError, throwable));
  }

}
