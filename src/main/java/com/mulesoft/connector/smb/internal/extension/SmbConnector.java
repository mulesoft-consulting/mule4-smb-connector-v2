package com.mulesoft.connector.smb.internal.extension;


import org.mule.extension.file.common.api.exceptions.FileError;
import org.mule.runtime.api.meta.Category;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;

@Extension(name = "SMB", category = Category.CERTIFIED)
@Xml(prefix = "smb")
@Configurations({SmbConfiguration.class})
@ErrorTypes(FileError.class)
public class SmbConnector {

}
