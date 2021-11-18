/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.error.provider;

import com.mulesoft.connector.smb.internal.error.SmbFileErrorType;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SmbFileCopyErrorTypeProvider  implements ErrorTypeProvider {

    public Set<ErrorTypeDefinition> getErrorTypes() {
        return Collections.unmodifiableSet(new HashSet<SmbFileErrorType>(Arrays.asList(SmbFileErrorType.ILLEGAL_PATH, SmbFileErrorType.FILE_ALREADY_EXISTS)));
    }
}
