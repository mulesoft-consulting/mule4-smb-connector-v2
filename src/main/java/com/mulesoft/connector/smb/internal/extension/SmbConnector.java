/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.extension;


import com.mulesoft.connector.smb.internal.config.SmbConfiguration;
import com.mulesoft.connector.smb.internal.error.SmbFileErrorType;
import org.mule.runtime.api.meta.Category;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.runtime.extension.api.annotation.license.RequiresEnterpriseLicense;

@Extension(name = "SMB", category = Category.CERTIFIED)
@Xml(prefix = "smb")
@Configurations({SmbConfiguration.class})
@ErrorTypes(SmbFileErrorType.class)
@RequiresEnterpriseLicense(allowEvaluationLicense = true)
public class SmbConnector {

}
