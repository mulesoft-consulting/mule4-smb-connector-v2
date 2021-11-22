/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.error;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.error.MuleErrors;

import java.util.Optional;

public enum SmbFileErrorType implements ErrorTypeDefinition<SmbFileErrorType> {
    FILE_NOT_FOUND,
    ILLEGAL_PATH,
    ILLEGAL_CONTENT,
    FILE_LOCK,
    FILE_ALREADY_EXISTS,
    ACCESS_DENIED,
    CONNECTIVITY(MuleErrors.CONNECTIVITY),
    FILE_DOESNT_EXIST(CONNECTIVITY),
    FILE_IS_NOT_DIRECTORY(CONNECTIVITY),
    INVALID_CREDENTIALS(CONNECTIVITY),
    CONNECTION_TIMEOUT(CONNECTIVITY),
    CANNOT_REACH(CONNECTIVITY),
    UNKNOWN_HOST(CONNECTIVITY),
    SERVICE_NOT_AVAILABLE(CONNECTIVITY),
    DISCONNECTED(CONNECTIVITY);

    private ErrorTypeDefinition<? extends Enum<?>> parentError;

    SmbFileErrorType(ErrorTypeDefinition<? extends Enum<?>> parentError) {
        this.parentError = parentError;
    }

    SmbFileErrorType() {
    }

    @Override
    public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
        return Optional.ofNullable(this.parentError);
    }
}
