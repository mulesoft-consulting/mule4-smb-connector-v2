/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
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
