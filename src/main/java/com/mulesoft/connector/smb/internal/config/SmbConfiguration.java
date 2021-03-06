/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.config;

import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.mule.extension.file.common.api.FileConnectorConfig;
import com.mulesoft.connector.smb.internal.connection.provider.SmbConnectionProvider;
import com.mulesoft.connector.smb.internal.operation.SmbOperations;
import com.mulesoft.connector.smb.internal.source.SmbDirectorySource;
import org.mule.runtime.core.api.connector.ConnectionManager;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * This is the main class of an extension, is the entry point from which
 * configurations, connection providers, operations and sources are going to be
 * declared.
 */
@Configuration
@ConnectionProviders({SmbConnectionProvider.class})
@Operations({SmbOperations.class})
@Sources(SmbDirectorySource.class)
public class SmbConfiguration extends FileConnectorConfig {

  @Inject
  private ConnectionManager connectionManager;

  /**
    * Wait time between size checks to determine if a file is ready to be read. This allows a file write to complete before
    * processing. If no value is provided, the check will not be performed. When enabled, Mule performs two size checks waiting the
    * specified time between calls. If both checks return the same value, the file is ready to be read.This attribute works in
    * tandem with {@link #timeBetweenSizeCheckUnit}.
    */
  @Parameter
  @Placement(tab = ADVANCED_TAB)
  @Summary("Wait time between size checks to determine if a file is ready to be read.")
  @Optional
  private Long timeBetweenSizeCheck;

  /**
   * A {@link TimeUnit} which qualifies the {@link #timeBetweenSizeCheck} attribute.
   * <p>
   * Defaults to {@code MILLISECONDS}
   */
  @Parameter
  @Placement(tab = ADVANCED_TAB)
  @Optional(defaultValue = "MILLISECONDS")
  @Summary("Time unit to be used in the wait time between size checks")
  private TimeUnit timeBetweenSizeCheckUnit;

  public ConnectionManager getConnectionManager() {
    return connectionManager;
  }
}
