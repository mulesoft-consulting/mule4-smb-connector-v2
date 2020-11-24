/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package org.mule.extension.smb;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.mule.extension.file.common.api.exceptions.FileError.CONNECTION_TIMEOUT;
import static org.mule.extension.file.common.api.exceptions.FileError.INVALID_CREDENTIALS;
import static org.mule.extension.file.common.api.exceptions.FileError.UNKNOWN_HOST;
import static org.mule.extension.smb.AllureConstants.SmbFeature.SMB_EXTENSION;
import static org.mule.functional.junit4.matchers.ThrowableCauseMatcher.hasCause;
import static org.mule.tck.junit4.matcher.ErrorTypeMatcher.errorType;
import org.mule.extension.smb.error.exception.SmbConnectionException;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.tck.util.TestConnectivityUtils;

import java.util.Arrays;
import java.util.Collection;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;

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
        {"smb", new org.mule.extension.smb.SmbTestHarness(), SMB_CONNECTION_XML}});
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

  @Test
  public void configMissingCredentials() {
    utils.assertFailedConnection(name + "ConfigMissingCredentials", ANYTHING, is(errorType(INVALID_CREDENTIALS)));
  }

  @Test
  public void configUnknownHost() {
    utils.assertFailedConnection(name + "ConfigUnknownHost", ANYTHING, is(errorType(UNKNOWN_HOST)));
  }

}
