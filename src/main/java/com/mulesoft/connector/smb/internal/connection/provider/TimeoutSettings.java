/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.provider;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.util.concurrent.TimeUnit;

import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;

/**
 * Groups timeout related parameters
 *
 * @since 1.0
 */
public final class TimeoutSettings {

  /**
   * A {@link TimeUnit} which qualifies the {@link #socketTimeout} attribute.
   * <p>
   * Defaults to {@code SECONDS}
   */
  @Parameter
  @Optional(defaultValue = "SECONDS")
  @Placement(tab = ADVANCED_TAB, order = 3)
  @Summary("Time unit to be used in the Socket Timeout")
  private TimeUnit socketTimeoutUnit;

  /**
   * A scalar value representing the amount of time to wait before a socket packet response times out. This attribute works in tandem
   * with {@link #socketTimeoutUnit}.
   * <p>
   * Defaults to {@code 10}
   */
  @Parameter
  @Optional(defaultValue = "10")
  @Placement(tab = ADVANCED_TAB, order = 4)
  @Summary("Socket timeout value")
  private Integer socketTimeout;

  /**
   * A {@link TimeUnit} which qualifies the {@link #readTimeout} attribute.
   * <p>
   * Defaults to {@code SECONDS}
   */
  @Parameter
  @Optional(defaultValue = "SECONDS")
  @Placement(tab = ADVANCED_TAB, order = 5)
  @Summary("Time unit to be used in the Read Timeout")
  private TimeUnit readTimeoutUnit;

  /**
   * A scalar value representing the amount of time to wait before a read operation times out. This attribute works in tandem
   * with {@link #readTimeoutUnit}.
   * <p>
   * Defaults to {@code 60}
   */
  @Parameter
  @Optional(defaultValue = "60")
  @Placement(tab = ADVANCED_TAB, order = 6)
  @Summary("Read operation timeout value")
  private Integer readTimeout;

  /**
   * A {@link TimeUnit} which qualifies the {@link #writeTimeout} attribute.
   * <p>
   * Defaults to {@code SECONDS}
   */
  @Parameter
  @Optional(defaultValue = "SECONDS")
  @Placement(tab = ADVANCED_TAB, order = 7)
  @Summary("Time unit to be used in the Write Timeout")
  private TimeUnit writeTimeoutUnit;

  /**
   * A scalar value representing the amount of time to wait before a write operation times out. This attribute works in tandem
   * with {@link #writeTimeoutUnit}.
   * <p>
   * Defaults to {@code 10}
   */
  @Parameter
  @Optional(defaultValue = "60")
  @Placement(tab = ADVANCED_TAB, order = 8)
  @Summary("Write operation timeout value")
  private Integer writeTimeout;

  /**
   * A {@link TimeUnit} which qualifies the {@link #transactionTimeout} attribute.
   * <p>
   * Defaults to {@code SECONDS}
   */
  @Parameter
  @Optional(defaultValue = "SECONDS")
  @Placement(tab = ADVANCED_TAB, order = 9)
  @Summary("Time unit to be used in the Transaction Timeout")
  private TimeUnit transactionTimeoutUnit;

  /**
   * A scalar value representing the amount of time to wait before a transaction (all operations except read and write) times out. This attribute works in tandem
   * with {@link #transactionTimeoutUnit}.
   * <p>
   * Defaults to {@code 10}
   */
  @Parameter
  @Optional(defaultValue = "60")
  @Placement(tab = ADVANCED_TAB, order = 10)
  @Summary("Transaction (operation other than read and write) timeout value")
  private Integer transactionTimeout;

  public TimeUnit getSocketTimeoutUnit() {
    return socketTimeoutUnit;
  }

  public Integer getSocketTimeout() {
    return socketTimeout;
  }

  public TimeUnit getReadTimeoutUnit() {
    return readTimeoutUnit;
  }

  public Integer getReadTimeout() {
    return readTimeout;
  }

  public TimeUnit getWriteTimeoutUnit() {
    return writeTimeoutUnit;
  }

  public Integer getWriteTimeout() {
    return writeTimeout;
  }

  public TimeUnit getTransactionTimeoutUnit() {
    return transactionTimeoutUnit;
  }

  public Integer getTransactionTimeout() {
    return transactionTimeout;
  }

}
