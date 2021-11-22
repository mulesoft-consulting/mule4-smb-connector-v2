/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.error.exception;

import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * {@link ConnectionException} implementation to declare connectivity errors in
 * the SmbConfiguration
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
