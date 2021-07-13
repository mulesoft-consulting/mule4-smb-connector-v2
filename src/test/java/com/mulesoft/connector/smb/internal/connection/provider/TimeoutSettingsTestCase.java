/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.provider;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TimeoutSettingsTestCase {

  @Test
  public void testGetters() {
    TimeoutSettings timeoutSettings = new TimeoutSettings();
    timeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    timeoutSettings.setSocketTimeout(10);
    timeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    timeoutSettings.setReadTimeout(20);
    timeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    timeoutSettings.setWriteTimeout(30);
    timeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    timeoutSettings.setTransactionTimeout(40);

    assertEquals(TimeUnit.MILLISECONDS, timeoutSettings.getSocketTimeoutUnit());
    assertEquals(Integer.valueOf(10), timeoutSettings.getSocketTimeout());

    assertEquals(TimeUnit.SECONDS, timeoutSettings.getReadTimeoutUnit());
    assertEquals(Integer.valueOf(20), timeoutSettings.getReadTimeout());

    assertEquals(TimeUnit.MINUTES, timeoutSettings.getWriteTimeoutUnit());
    assertEquals(Integer.valueOf(30), timeoutSettings.getWriteTimeout());

    assertEquals(TimeUnit.HOURS, timeoutSettings.getTransactionTimeoutUnit());
    assertEquals(Integer.valueOf(40), timeoutSettings.getTransactionTimeout());


  }

  @Test
  public void testTimeoutSettingsEqualsSameObject() {
    TimeoutSettings timeoutSettings = new TimeoutSettings();
    assertEquals(timeoutSettings, timeoutSettings);
  }

  @Test
  public void testTimeoutSettingsEqualsNullObject() {
    TimeoutSettings timeoutSettings = new TimeoutSettings();
    timeoutSettings.setSocketTimeout(10);
    timeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);

    assertNotEquals(timeoutSettings, null);
  }

  @Test
  public void testTimeoutSettingsEqualsOtherClassObject() {
    TimeoutSettings timeoutSettings = new TimeoutSettings();
    timeoutSettings.setSocketTimeout(10);
    timeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);

    assertNotEquals(timeoutSettings, "");
  }

  @Test
  public void testTimeoutSettingsEqualsOtherSettingsSameValues() {
    TimeoutSettings oneTimeoutSettings = new TimeoutSettings();
    oneTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    oneTimeoutSettings.setSocketTimeout(10);
    oneTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    oneTimeoutSettings.setReadTimeout(20);
    oneTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    oneTimeoutSettings.setWriteTimeout(30);
    oneTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    oneTimeoutSettings.setTransactionTimeout(40);

    TimeoutSettings otherTimeoutSettings = new TimeoutSettings();
    otherTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    otherTimeoutSettings.setSocketTimeout(10);
    otherTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    otherTimeoutSettings.setReadTimeout(20);
    otherTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    otherTimeoutSettings.setWriteTimeout(30);
    otherTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    otherTimeoutSettings.setTransactionTimeout(40);

    assertEquals(oneTimeoutSettings, otherTimeoutSettings);
  }

  @Test
  public void testTimeoutSettingsEqualsOtherDiffSocketTimeoutUnit() {
    TimeoutSettings oneTimeoutSettings = new TimeoutSettings();
    oneTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    oneTimeoutSettings.setSocketTimeout(10);
    oneTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    oneTimeoutSettings.setReadTimeout(20);
    oneTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    oneTimeoutSettings.setWriteTimeout(30);
    oneTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    oneTimeoutSettings.setTransactionTimeout(40);

    TimeoutSettings otherTimeoutSettings = new TimeoutSettings();
    otherTimeoutSettings.setSocketTimeoutUnit(TimeUnit.SECONDS);
    otherTimeoutSettings.setSocketTimeout(10);
    otherTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    otherTimeoutSettings.setReadTimeout(20);
    otherTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    otherTimeoutSettings.setWriteTimeout(30);
    otherTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    otherTimeoutSettings.setTransactionTimeout(40);

    assertNotEquals(oneTimeoutSettings, otherTimeoutSettings);
  }

  @Test
  public void testTimeoutSettingsEqualsOtherDiffSocketTimeout() {
    TimeoutSettings oneTimeoutSettings = new TimeoutSettings();
    oneTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    oneTimeoutSettings.setSocketTimeout(10);
    oneTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    oneTimeoutSettings.setReadTimeout(20);
    oneTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    oneTimeoutSettings.setWriteTimeout(30);
    oneTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    oneTimeoutSettings.setTransactionTimeout(40);

    TimeoutSettings otherTimeoutSettings = new TimeoutSettings();
    otherTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    otherTimeoutSettings.setSocketTimeout(15);
    otherTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    otherTimeoutSettings.setReadTimeout(20);
    otherTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    otherTimeoutSettings.setWriteTimeout(30);
    otherTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    otherTimeoutSettings.setTransactionTimeout(40);

    assertNotEquals(oneTimeoutSettings, otherTimeoutSettings);
  }

  @Test
  public void testTimeoutSettingsEqualsOtherDiffReadTimeoutUnit() {
    TimeoutSettings oneTimeoutSettings = new TimeoutSettings();
    oneTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    oneTimeoutSettings.setSocketTimeout(10);
    oneTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    oneTimeoutSettings.setReadTimeout(20);
    oneTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    oneTimeoutSettings.setWriteTimeout(30);
    oneTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    oneTimeoutSettings.setTransactionTimeout(40);

    TimeoutSettings otherTimeoutSettings = new TimeoutSettings();
    otherTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    otherTimeoutSettings.setSocketTimeout(10);
    otherTimeoutSettings.setReadTimeoutUnit(TimeUnit.MINUTES);
    otherTimeoutSettings.setReadTimeout(20);
    otherTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    otherTimeoutSettings.setWriteTimeout(30);
    otherTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    otherTimeoutSettings.setTransactionTimeout(40);

    assertNotEquals(oneTimeoutSettings, otherTimeoutSettings);
  }

  @Test
  public void testTimeoutSettingsEqualsOtherDiffReadTimeout() {
    TimeoutSettings oneTimeoutSettings = new TimeoutSettings();
    oneTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    oneTimeoutSettings.setSocketTimeout(10);
    oneTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    oneTimeoutSettings.setReadTimeout(20);
    oneTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    oneTimeoutSettings.setWriteTimeout(30);
    oneTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    oneTimeoutSettings.setTransactionTimeout(40);

    TimeoutSettings otherTimeoutSettings = new TimeoutSettings();
    otherTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    otherTimeoutSettings.setSocketTimeout(10);
    otherTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    otherTimeoutSettings.setReadTimeout(25);
    otherTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    otherTimeoutSettings.setWriteTimeout(30);
    otherTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    otherTimeoutSettings.setTransactionTimeout(40);

    assertNotEquals(oneTimeoutSettings, otherTimeoutSettings);
  }


  @Test
  public void testTimeoutSettingsEqualsOtherDiffWriteTimeoutUnit() {
    TimeoutSettings oneTimeoutSettings = new TimeoutSettings();
    oneTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    oneTimeoutSettings.setSocketTimeout(10);
    oneTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    oneTimeoutSettings.setReadTimeout(20);
    oneTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    oneTimeoutSettings.setWriteTimeout(30);
    oneTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    oneTimeoutSettings.setTransactionTimeout(40);

    TimeoutSettings otherTimeoutSettings = new TimeoutSettings();
    otherTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    otherTimeoutSettings.setSocketTimeout(10);
    otherTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    otherTimeoutSettings.setReadTimeout(20);
    otherTimeoutSettings.setWriteTimeoutUnit(TimeUnit.HOURS);
    otherTimeoutSettings.setWriteTimeout(30);
    otherTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    otherTimeoutSettings.setTransactionTimeout(40);

    assertNotEquals(oneTimeoutSettings, otherTimeoutSettings);
  }

  @Test
  public void testTimeoutSettingsEqualsOtherDiffWriteTimeout() {
    TimeoutSettings oneTimeoutSettings = new TimeoutSettings();
    oneTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    oneTimeoutSettings.setSocketTimeout(10);
    oneTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    oneTimeoutSettings.setReadTimeout(20);
    oneTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    oneTimeoutSettings.setWriteTimeout(30);
    oneTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    oneTimeoutSettings.setTransactionTimeout(40);

    TimeoutSettings otherTimeoutSettings = new TimeoutSettings();
    otherTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    otherTimeoutSettings.setSocketTimeout(10);
    otherTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    otherTimeoutSettings.setReadTimeout(20);
    otherTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    otherTimeoutSettings.setWriteTimeout(35);
    otherTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    otherTimeoutSettings.setTransactionTimeout(40);

    assertNotEquals(oneTimeoutSettings, otherTimeoutSettings);
  }

  @Test
  public void testTimeoutSettingsEqualsOtherDiffTransactionTimeoutUnit() {
    TimeoutSettings oneTimeoutSettings = new TimeoutSettings();
    oneTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    oneTimeoutSettings.setSocketTimeout(10);
    oneTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    oneTimeoutSettings.setReadTimeout(20);
    oneTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    oneTimeoutSettings.setWriteTimeout(30);
    oneTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    oneTimeoutSettings.setTransactionTimeout(40);

    TimeoutSettings otherTimeoutSettings = new TimeoutSettings();
    otherTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    otherTimeoutSettings.setSocketTimeout(10);
    otherTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    otherTimeoutSettings.setReadTimeout(20);
    otherTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    otherTimeoutSettings.setWriteTimeout(30);
    otherTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.DAYS);
    otherTimeoutSettings.setTransactionTimeout(40);

    assertNotEquals(oneTimeoutSettings, otherTimeoutSettings);
  }

  @Test
  public void testTimeoutSettingsEqualsOtherDiffTransactionTimeout() {
    TimeoutSettings oneTimeoutSettings = new TimeoutSettings();
    oneTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    oneTimeoutSettings.setSocketTimeout(10);
    oneTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    oneTimeoutSettings.setReadTimeout(20);
    oneTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    oneTimeoutSettings.setWriteTimeout(30);
    oneTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    oneTimeoutSettings.setTransactionTimeout(40);

    TimeoutSettings otherTimeoutSettings = new TimeoutSettings();
    otherTimeoutSettings.setSocketTimeoutUnit(TimeUnit.MILLISECONDS);
    otherTimeoutSettings.setSocketTimeout(10);
    otherTimeoutSettings.setReadTimeoutUnit(TimeUnit.SECONDS);
    otherTimeoutSettings.setReadTimeout(20);
    otherTimeoutSettings.setWriteTimeoutUnit(TimeUnit.MINUTES);
    otherTimeoutSettings.setWriteTimeout(30);
    otherTimeoutSettings.setTransactionTimeoutUnit(TimeUnit.HOURS);
    otherTimeoutSettings.setTransactionTimeout(45);

    assertNotEquals(oneTimeoutSettings, otherTimeoutSettings);
  }



}
