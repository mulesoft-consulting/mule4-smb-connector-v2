/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.api;

import org.mule.extension.file.common.api.matcher.FileMatcher;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static java.time.LocalDateTime.now;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A set of criterias used to filter files stored in a SMB server. The file's properties are to be represented on
 * an instance of {@link SmbFileAttributes}.
 *
 * @since 1.0
 */
@Alias("matcher")
@TypeDsl(allowTopLevelDefinition = true)
public class SmbFileMatcher extends FileMatcher<SmbFileMatcher, SmbFileAttributes> {

  private static final Logger logger = getLogger(SmbFileMatcher.class);
  private final AtomicBoolean alreadyLoggedWarning = new AtomicBoolean();

  /**
   * Files created before this date are rejected.
   */
  @Parameter
  @Summary("Files created before this date are rejected.")
  @Example("2015-06-03T13:21:58+00:00")
  @Optional
  private LocalDateTime timestampSince;

  /**
   * Files created after this date are rejected.
   */
  @Parameter
  @Summary("Files created after this date are rejected.")
  @Example("2015-06-03T13:21:58+00:00")
  @Optional
  private LocalDateTime timestampUntil;

  /**
   * Minimum time that should have passed since a file was updated to not be rejected. This attribute works in tandem with {@link #timeUnit}.
   */
  @Parameter
  @Summary("Minimum time that should have passed since a file was updated to not be rejected. This attribute works in tandem with timeUnit.")
  @Example("10000")
  @Optional
  private Long notUpdatedInTheLast;

  /**
   * Maximum time that should have passed since a file was updated to not be rejected. This attribute works in tandem with {@link #timeUnit}.
   */
  @Parameter
  @Summary("Maximum time that should have passed since a file was updated to not be rejected. This attribute works in tandem with timeUnit.")
  @Example("10000")
  @Optional
  private Long updatedInTheLast;

  /**
   * A {@link TimeUnit} which qualifies the {@link #updatedInTheLast} and the {@link #notUpdatedInTheLast} attributes.
   * <p>
   * Defaults to {@code MILLISECONDS}
   */
  @Parameter
  @Summary("Time unit to be used to interpret the parameters 'notUpdatedInTheLast' and 'updatedInTheLast'")
  @Optional(defaultValue = "MILLISECONDS")
  private TimeUnit timeUnit;

  @Override
  protected Predicate<SmbFileAttributes> addConditions(Predicate<SmbFileAttributes> predicate) {
    Predicate<SmbFileAttributes> result = predicate;

    if (timestampSince != null) {
      result = result.and(attributes -> FILE_TIME_SINCE.apply(timestampSince, attributes.getTimestamp()));
    }

    if (timestampUntil != null) {
      result = result.and(attributes -> FILE_TIME_UNTIL.apply(timestampUntil, attributes.getTimestamp()));
    }

    LocalDateTime referenceDateTime = now();

    if (notUpdatedInTheLast != null) {
      result = result.and(attributes -> {
        checkTimestampPrecision(attributes);
        return FILE_TIME_UNTIL.apply(minusTime(referenceDateTime, notUpdatedInTheLast, timeUnit), attributes.getTimestamp());
      });
    }

    if (updatedInTheLast != null) {
      result = result.and(attributes -> {
        checkTimestampPrecision(attributes);
        return FILE_TIME_SINCE.apply(minusTime(referenceDateTime, updatedInTheLast, timeUnit), attributes.getTimestamp());
      });
    }

    return result;
  }

  private void checkTimestampPrecision(SmbFileAttributes attributes) {
    if (alreadyLoggedWarning.compareAndSet(false, true) && isSecondsOrLower(timeUnit)
        && attributes.getTimestamp().getSecond() == 0 && attributes.getTimestamp().getNano() == 0) {
      logger
          .warn("The required timestamp precision {}} cannot be met. The server may not support it.",
                timeUnit);
    }
  }

  private boolean isSecondsOrLower(TimeUnit timeUnit) {
    return timeUnit == TimeUnit.SECONDS || timeUnit == TimeUnit.MILLISECONDS || timeUnit == TimeUnit.MICROSECONDS
        || timeUnit == TimeUnit.NANOSECONDS;
  }

  private LocalDateTime minusTime(LocalDateTime localDateTime, Long time, TimeUnit timeUnit) {
    return localDateTime.minus(getTimeInMillis(time, timeUnit), ChronoUnit.MILLIS);
  }

  private long getTimeInMillis(Long time, TimeUnit timeUnit) {
    return timeUnit.toMillis(time);
  }

  public LocalDateTime getTimestampSince() {
    return timestampSince;
  }

  public LocalDateTime getTimestampUntil() {
    return timestampUntil;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  public Long getUpdatedInTheLast() {
    return updatedInTheLast;
  }

  public Long getNotUpdatedInTheLast() {
    return notUpdatedInTheLast;
  }


}
