/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.api;

import com.mulesoft.connector.smb.AllureConstants;
import io.qameta.allure.Feature;
import org.junit.Before;
import org.junit.Test;
import org.mule.extension.file.common.api.matcher.MatchPolicy;
import org.mule.test.extension.file.common.FileMatcherContractTestCase;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mule.extension.file.common.api.matcher.MatchPolicy.INCLUDE;
import static org.mule.extension.file.common.api.matcher.MatchPolicy.REQUIRE;

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

  @Test
  public void notUpdatedInTheLast() {
    builder.setNotUpdatedInTheLast(1000L);
    builder.setTimeUnit(TimeUnit.MILLISECONDS);
    when(attributes.getTimestamp()).thenReturn(LocalDateTime.now().minus(10000, ChronoUnit.MILLIS));
    assertMatch();
  }

  @Test
  public void rejectNotUpdatedInTheLast() {
    builder.setNotUpdatedInTheLast(10000L);
    builder.setTimeUnit(TimeUnit.MILLISECONDS);
    when(attributes.getTimestamp()).thenReturn(LocalDateTime.now());
    assertReject();
  }

  @Test
  public void updatedInTheLast() {
    builder.setUpdatedInTheLast(10000L);
    builder.setTimeUnit(TimeUnit.MILLISECONDS);
    when(attributes.getTimestamp()).thenReturn(LocalDateTime.now());
    assertMatch();
  }

  @Test
  public void rejectUpdatedInTheLast() {
    builder.setUpdatedInTheLast(1000L);
    builder.setTimeUnit(TimeUnit.MILLISECONDS);
    when(attributes.getTimestamp()).thenReturn(LocalDateTime.now().minus(10000, ChronoUnit.MILLIS));
    assertReject();
  }

  @Test
  public void testGetters() {
    LocalDateTime time1 = LocalDateTime.now().minus(10000, ChronoUnit.MILLIS);
    LocalDateTime time2 = LocalDateTime.now().minus(9000, ChronoUnit.MILLIS);
    SmbFileMatcher matcher = new SmbFileMatcher();
    matcher.setUpdatedInTheLast(1000L);
    matcher.setTimestampSince(time1);
    matcher.setTimestampUntil(time2);
    matcher.setNotUpdatedInTheLast(4000L);
    matcher.setTimeUnit(TimeUnit.MILLISECONDS);
    matcher.setDirectories(MatchPolicy.EXCLUDE);
    matcher.setFilenamePattern("someFilePattern");
    matcher.setMaxSize(1000L);
    matcher.setMinSize(2000L);
    matcher.setPathPattern("pathPattern");
    matcher.setRegularFiles(REQUIRE);
    matcher.setSymLinks(INCLUDE);

    assertEquals(Long.valueOf(1000), matcher.getUpdatedInTheLast());
    assertEquals(time1, matcher.getTimestampSince());
    assertEquals(time2, matcher.getTimestampUntil());
    assertEquals(Long.valueOf(4000), matcher.getNotUpdatedInTheLast());
    assertEquals(TimeUnit.MILLISECONDS, matcher.getTimeUnit());
    assertEquals(MatchPolicy.EXCLUDE, matcher.getDirectories());
    assertEquals("someFilePattern", matcher.getFilenamePattern());
    assertEquals(Long.valueOf(1000), matcher.getMaxSize());
    assertEquals(Long.valueOf(2000), matcher.getMinSize());
    assertEquals("pathPattern", matcher.getPathPattern());
    assertEquals(REQUIRE, matcher.getRegularFiles());
    assertEquals(INCLUDE, matcher.getSymLinks());



  }

}
