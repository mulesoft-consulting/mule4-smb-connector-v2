/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.provider;

import org.junit.Test;

import static org.junit.Assert.*;

public class SmbConnectionSettingsTestCase {

  @Test
  public void testGetters() {
    SmbConnectionSettings connSettings = new SmbConnectionSettings();
    connSettings.setDomain("domain");
    connSettings.setHost("host");
    connSettings.setPort(445);
    connSettings.setUsername("usr");
    connSettings.setPassword("pwd");
    connSettings.setShareRoot("shareRoot");
    connSettings.setDfsEnabled(true);

    assertEquals("domain", connSettings.getDomain());
    assertEquals("host", connSettings.getHost());
    assertEquals(445, connSettings.getPort());
    assertEquals("usr", connSettings.getUsername());
    assertEquals("pwd", connSettings.getPassword());
    assertEquals("shareRoot", connSettings.getShareRoot());
    assertTrue(connSettings.isDfsEnabled());
  }

  @Test
  public void connSettingsEqualsNullObject() {
    SmbConnectionSettings connSettings = new SmbConnectionSettings();
    assertNotEquals(connSettings, null);
  }

  @Test
  public void connSettingsEqualsOtherClassObject() {
    SmbConnectionSettings connSettings = new SmbConnectionSettings();
    assertNotEquals(connSettings, "");
  }

  @Test
  public void connSettingsEqualsSameObject() {
    SmbConnectionSettings connSettings = new SmbConnectionSettings();
    assertNotEquals(connSettings, connSettings);
  }

  @Test
  public void connSettingsEqualsOtherConnSettingsSameValues() {
    SmbConnectionSettings oneConnSettings = new SmbConnectionSettings();
    oneConnSettings.setDomain("domain");
    oneConnSettings.setHost("host");
    oneConnSettings.setPort(445);
    oneConnSettings.setUsername("usr");
    oneConnSettings.setPassword("pwd");
    oneConnSettings.setShareRoot("shareRoot");
    oneConnSettings.setDfsEnabled(true);

    SmbConnectionSettings otherConnSettings = new SmbConnectionSettings();
    otherConnSettings.setDomain("domain");
    otherConnSettings.setHost("host");
    otherConnSettings.setPort(445);
    otherConnSettings.setUsername("usr");
    otherConnSettings.setPassword("pwd");
    otherConnSettings.setShareRoot("shareRoot");
    otherConnSettings.setDfsEnabled(true);

    assertEquals(oneConnSettings, otherConnSettings);
  }

  @Test
  public void connSettingsEqualsOtherConnSettingsDiffDomain() {
    SmbConnectionSettings oneConnSettings = new SmbConnectionSettings();
    oneConnSettings.setDomain("domain");
    oneConnSettings.setHost("host");
    oneConnSettings.setPort(445);
    oneConnSettings.setUsername("usr");
    oneConnSettings.setPassword("pwd");
    oneConnSettings.setShareRoot("shareRoot");
    oneConnSettings.setDfsEnabled(true);

    SmbConnectionSettings otherConnSettings = new SmbConnectionSettings();
    otherConnSettings.setDomain("otherDomain");
    otherConnSettings.setHost("host");
    otherConnSettings.setPort(445);
    otherConnSettings.setUsername("usr");
    otherConnSettings.setPassword("pwd");
    otherConnSettings.setShareRoot("shareRoot");
    otherConnSettings.setDfsEnabled(true);

    assertNotEquals(oneConnSettings, otherConnSettings);
  }

  @Test
  public void connSettingsEqualsOtherConnSettingsDiffHost() {
    SmbConnectionSettings oneConnSettings = new SmbConnectionSettings();
    oneConnSettings.setDomain("domain");
    oneConnSettings.setHost("host");
    oneConnSettings.setPort(445);
    oneConnSettings.setUsername("usr");
    oneConnSettings.setPassword("pwd");
    oneConnSettings.setShareRoot("shareRoot");
    oneConnSettings.setDfsEnabled(true);

    SmbConnectionSettings otherConnSettings = new SmbConnectionSettings();
    otherConnSettings.setDomain("domain");
    otherConnSettings.setHost("otherhost");
    otherConnSettings.setPort(445);
    otherConnSettings.setUsername("usr");
    otherConnSettings.setPassword("pwd");
    otherConnSettings.setShareRoot("shareRoot");
    otherConnSettings.setDfsEnabled(true);

    assertNotEquals(oneConnSettings, otherConnSettings);
  }

  @Test
  public void connSettingsEqualsOtherConnSettingsDiffPort() {
    SmbConnectionSettings oneConnSettings = new SmbConnectionSettings();
    oneConnSettings.setDomain("domain");
    oneConnSettings.setHost("host");
    oneConnSettings.setPort(445);
    oneConnSettings.setUsername("usr");
    oneConnSettings.setPassword("pwd");
    oneConnSettings.setShareRoot("shareRoot");
    oneConnSettings.setDfsEnabled(true);

    SmbConnectionSettings otherConnSettings = new SmbConnectionSettings();
    otherConnSettings.setDomain("domain");
    otherConnSettings.setHost("host");
    otherConnSettings.setPort(446);
    otherConnSettings.setUsername("usr");
    otherConnSettings.setPassword("pwd");
    otherConnSettings.setShareRoot("shareRoot");
    otherConnSettings.setDfsEnabled(true);

    assertNotEquals(oneConnSettings, otherConnSettings);
  }

  @Test
  public void connSettingsEqualsOtherConnSettingsDiffUser() {
    SmbConnectionSettings oneConnSettings = new SmbConnectionSettings();
    oneConnSettings.setDomain("domain");
    oneConnSettings.setHost("host");
    oneConnSettings.setPort(445);
    oneConnSettings.setUsername("usr");
    oneConnSettings.setPassword("pwd");
    oneConnSettings.setShareRoot("shareRoot");
    oneConnSettings.setDfsEnabled(true);

    SmbConnectionSettings otherConnSettings = new SmbConnectionSettings();
    otherConnSettings.setDomain("domain");
    otherConnSettings.setHost("host");
    otherConnSettings.setPort(445);
    otherConnSettings.setUsername("otherusr");
    otherConnSettings.setPassword("pwd");
    otherConnSettings.setShareRoot("shareRoot");
    otherConnSettings.setDfsEnabled(true);

    assertNotEquals(oneConnSettings, otherConnSettings);
  }

  @Test
  public void connSettingsEqualsOtherConnSettingsDiffPassword() {
    SmbConnectionSettings oneConnSettings = new SmbConnectionSettings();
    oneConnSettings.setDomain("domain");
    oneConnSettings.setHost("host");
    oneConnSettings.setPort(445);
    oneConnSettings.setUsername("usr");
    oneConnSettings.setPassword("pwd");
    oneConnSettings.setShareRoot("shareRoot");
    oneConnSettings.setDfsEnabled(true);

    SmbConnectionSettings otherConnSettings = new SmbConnectionSettings();
    otherConnSettings.setDomain("domain");
    otherConnSettings.setHost("host");
    otherConnSettings.setPort(445);
    otherConnSettings.setUsername("usr");
    otherConnSettings.setPassword("otherpwd");
    otherConnSettings.setShareRoot("shareRoot");
    otherConnSettings.setDfsEnabled(true);

    assertNotEquals(oneConnSettings, otherConnSettings);
  }

  @Test
  public void connSettingsEqualsOtherConnSettingsDiffShareRoot() {
    SmbConnectionSettings oneConnSettings = new SmbConnectionSettings();
    oneConnSettings.setDomain("domain");
    oneConnSettings.setHost("host");
    oneConnSettings.setPort(445);
    oneConnSettings.setUsername("usr");
    oneConnSettings.setPassword("pwd");
    oneConnSettings.setShareRoot("shareRoot");
    oneConnSettings.setDfsEnabled(true);

    SmbConnectionSettings otherConnSettings = new SmbConnectionSettings();
    otherConnSettings.setDomain("domain");
    otherConnSettings.setHost("host");
    otherConnSettings.setPort(445);
    otherConnSettings.setUsername("usr");
    otherConnSettings.setPassword("pwd");
    otherConnSettings.setShareRoot("otherShareRoot");
    otherConnSettings.setDfsEnabled(true);

    assertNotEquals(oneConnSettings, otherConnSettings);
  }

  @Test
  public void connSettingsEqualsOtherConnSettingsDiffDfsEnabled() {
    SmbConnectionSettings oneConnSettings = new SmbConnectionSettings();
    oneConnSettings.setDomain("domain");
    oneConnSettings.setHost("host");
    oneConnSettings.setPort(445);
    oneConnSettings.setUsername("usr");
    oneConnSettings.setPassword("pwd");
    oneConnSettings.setShareRoot("shareRoot");
    oneConnSettings.setDfsEnabled(true);

    SmbConnectionSettings otherConnSettings = new SmbConnectionSettings();
    otherConnSettings.setDomain("domain");
    otherConnSettings.setHost("host");
    otherConnSettings.setPort(445);
    otherConnSettings.setUsername("usr");
    otherConnSettings.setPassword("pwd");
    otherConnSettings.setShareRoot("shareRoot");
    otherConnSettings.setDfsEnabled(false);

    assertNotEquals(oneConnSettings, otherConnSettings);
  }



}
