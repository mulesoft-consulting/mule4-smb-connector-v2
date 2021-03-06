/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector;

import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.extension.file.common.api.FileTestHarness;

import static org.mule.functional.api.exception.ExpectedError.none;

/**
 * Base class for {@link FileTestHarness} implementations
 *
 * @since 1.0
 */
public abstract class AbstractSmbTestHarness extends ExternalResource implements FileTestHarness {

  private final SystemProperty profileSystemProperty;
  private ExpectedError expectedError = none();

  /**
   * Creates a new instance
   *
   * @param profile the name of a spring profile to activate
   */
  public AbstractSmbTestHarness(String profile) {
    profileSystemProperty = new SystemProperty("spring.profiles.active", profile);
  }

  @Override
  public final Statement apply(Statement base, Description description) {
    base = applyAll(base, description, profileSystemProperty, expectedError);
    base = applyAll(base, description, getChildRules());
    return super.apply(base, description);
  }

  /**
   * @return {@link TestRule testRules} declared on the implementations which should also be applied
   */
  protected abstract TestRule[] getChildRules();

  @Override
  protected final void before() throws Throwable {
    doBefore();
  }

  /**
   * Template method for performing setup actions
   */
  protected void doBefore() throws Throwable {

  }

  /**
   * Delegates into {@link #doAfter()} and resets the {@link #expectedError}
   */
  @Override
  protected final void after() {
    try {
      doAfter();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      expectedError = none();
    }
  }

  /**
   * Template method for performing cleanup actions
   */
  protected void doAfter() {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExpectedError expectedError() {
    return expectedError;
  }

  private Statement applyAll(Statement base, Description description, TestRule... rules) {
    for (TestRule rule : rules) {
      base = rule.apply(base, description);
    }

    return base;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write(String folder, String fileName, String content) throws Exception {
    write(String.format("%s/%s", folder, fileName), content);
  }
}
