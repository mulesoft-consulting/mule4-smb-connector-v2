package com.mulesoft.connector.smb.internal.error.provider;

import com.mulesoft.connector.smb.internal.error.SmbFileError;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SmbFileWriteErrorTypeProvider implements ErrorTypeProvider {

    public Set<ErrorTypeDefinition> getErrorTypes() {
        return Collections.unmodifiableSet(new HashSet(Arrays.asList(SmbFileError.ILLEGAL_PATH, SmbFileError.ILLEGAL_CONTENT, SmbFileError.FILE_ALREADY_EXISTS, SmbFileError.ACCESS_DENIED, SmbFileError.FILE_LOCK)));
    }


}
