/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb;

import static org.mockito.Mockito.when;
import static org.mule.extension.file.common.api.matcher.MatchPolicy.INCLUDE;
import static org.mule.extension.file.common.api.matcher.MatchPolicy.REQUIRE;

import com.mulesoft.connector.smb.api.SmbFileAttributes;
import com.mulesoft.connector.smb.api.SmbFileMatcher;
import org.mule.test.extension.file.common.FileMatcherContractTestCase;

import java.time.LocalDateTime;

import io.qameta.allure.Feature;
import org.junit.Before;
import org.junit.Test;

@Feature(AllureConstants.SmbFeature.SMB_EXTENSION)
public class SmbFileMatcherTestCase
    extends FileMatcherContractTestCase<SmbFileMatcher, SmbFileAttributes> {

  private static final LocalDateTime TIMESTAMP = LocalDateTime.of(1983, 4, 20, 21, 15);

  @Override
  protected SmbFileMatcher createPredicateBuilder() {
    return new SmbFileMatcher();
  }

  @Override
  protected Class<SmbFileAttributes> getFileAttributesClass() {
    return SmbFileAttributes.class;
  }

  @Before
  @Override
  public void before() {
    super.before();
    when(attributes.getTimestamp()).thenReturn(TIMESTAMP);
  }

  @Test
  public void matchesAll() {
    builder.setFilenamePattern("glob:*.{java, js}").setPathPattern("glob:**.{java, js}")
        .setTimestampSince(LocalDateTime.of(1980, 1, 1, 0, 0))
        .setTimestampUntil(LocalDateTime.of(1990, 1, 1, 0, 0))
        .setRegularFiles(REQUIRE)
        .setDirectories(INCLUDE)
        .setSymLinks(INCLUDE)
        .setMinSize(1L)
        .setMaxSize(1024L);

    assertMatch();
  }

  @Test
  public void timestampSince() {
    builder.setTimestampSince(LocalDateTime.of(1980, 1, 1, 0, 0));
    assertMatch();
  }

  @Test
  public void timestampUntil() {
    builder.setTimestampUntil(LocalDateTime.of(1990, 1, 1, 0, 0));
    assertMatch();
  }

  @Test
  public void rejectTimestampSince() {
    builder.setTimestampSince(LocalDateTime.of(1984, 1, 1, 0, 0));
    assertReject();
  }

  @Test
  public void rejectTimestampUntil() {
    builder.setTimestampUntil(LocalDateTime.of(1982, 4, 2, 0, 0));
    assertReject();
  }
}
