package com.mulesoft.connector.smb.internal.error.provider;

import com.mulesoft.connector.smb.internal.error.SmbFileError;
import org.mule.extension.file.common.api.exceptions.FileCopyErrorTypeProvider;
import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SmbFileCopyErrorTypeProvider  implements ErrorTypeProvider {
    public SmbFileCopyErrorTypeProvider() {
    }

    public Set<ErrorTypeDefinition> getErrorTypes() {
        return Collections.unmodifiableSet(new HashSet(Arrays.asList(SmbFileError.ILLEGAL_PATH, SmbFileError.FILE_ALREADY_EXISTS)));
    }
}
