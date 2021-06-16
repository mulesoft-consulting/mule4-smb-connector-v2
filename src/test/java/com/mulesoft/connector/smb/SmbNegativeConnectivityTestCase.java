/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb;

import com.mulesoft.connector.smb.internal.error.exception.SmbConnectionException;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.tck.util.TestConnectivityUtils;

import java.util.Arrays;
import java.util.Collection;

import static com.mulesoft.connector.smb.AllureConstants.SmbFeature.SMB_EXTENSION;
import static org.hamcrest.CoreMatchers.*;
import static org.mule.extension.file.common.api.exceptions.FileError.*;
import static org.mule.functional.junit4.matchers.ThrowableCauseMatcher.hasCause;
import static org.mule.tck.junit4.matcher.ErrorTypeMatcher.errorType;

@Feature(SMB_EXTENSION)
@Story("Negative Connectivity Testing")
public class SmbNegativeConnectivityTestCase extends CommonSmbConnectorTestCase {

  private static final Matcher<Exception> ANYTHING =
      is(allOf(instanceOf(ConnectionException.class), hasCause(instanceOf(SmbConnectionException.class))));
  private final String name;
  private TestConnectivityUtils utils;

  @Rule
  public SystemProperty rule = TestConnectivityUtils.disableAutomaticTestConnectivity();

  public SmbNegativeConnectivityTestCase(String name, SmbTestHarness testHarness, String smbConfigFile) {
    super(name, testHarness, smbConfigFile);
    this.name = name;
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"smb", new SmbTestHarness(), SMB_CONNECTION_XML}});
  }

  @Override
  protected String getConfigFile() {
    return name + "-negative-connectivity-test.xml";
  }

  @Before
  public void setUp() {
    utils = new TestConnectivityUtils(registry);
  }

  @Test
  public void configInvalidCredentials() {
    utils.assertFailedConnection(name + "ConfigInvalidCredentials", ANYTHING, is(errorType(INVALID_CREDENTIALS)));
  }

  @Test
  public void configConnectionTimeout() {
    utils.assertFailedConnection(name + "ConfigConnectionTimeout", ANYTHING, is(errorType(CONNECTION_TIMEOUT)));
  }

  /*
  @Test
  @Ignore
  //TODO olamiral: if missing credentials, should try as anonymous.
  //Need to detail the unit tests regarding anonymous access (both positive and negative).
  //For now, this negative test will be ignored
  public void configMissingCredentials() {
    utils.assertFailedConnection(name + "ConfigMissingCredentials", ANYTHING, is(errorType(INVALID_CREDENTIALS)));
  }
  */

  @Test
  public void configUnknownHost() {
    utils.assertFailedConnection(name + "ConfigUnknownHost", ANYTHING, is(errorType(UNKNOWN_HOST)));
  }

}
