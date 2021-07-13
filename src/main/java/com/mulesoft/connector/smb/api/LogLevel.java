/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.api;

import org.slf4j.Logger;

public enum LogLevel {
  ERROR {

    @Override
    public void log(Logger logger, String message) {
      logger.error(message);
    }

    @Override
    public boolean isEnabled(Logger logger) {
      return logger.isErrorEnabled();
    }
  },
  WARN {

    @Override
    public void log(Logger logger, String message) {
      logger.warn(message);
    }

    @Override
    public boolean isEnabled(Logger logger) {
      return logger.isWarnEnabled();
    }
  },
  INFO {

    @Override
    public void log(Logger logger, String message) {
      logger.info(message);
    }

    @Override
    public boolean isEnabled(Logger logger) {
      return logger.isInfoEnabled();
    }
  },
  DEBUG {

    @Override
    public void log(Logger logger, String message) {
      logger.debug(message);
    }

    @Override
    public boolean isEnabled(Logger logger) {
      return logger.isDebugEnabled();
    }
  },
  TRACE {

    @Override
    public void log(Logger logger, String message) {
      logger.trace(message);
    }

    @Override
    public boolean isEnabled(Logger logger) {
      return logger.isTraceEnabled();
    }
  };

  public abstract void log(Logger logger, String message);

  public abstract boolean isEnabled(Logger logger);
}
