package org.mule.extension.smb.api;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import org.slf4j.Logger;

public enum LogLevel {
    ERROR {
        @Override
        public void log(Logger logger, Object object) {
            logger.error(object == null ? null : object.toString());
        }

        @Override
        public boolean isEnabled(Logger logger) {
            return logger.isErrorEnabled();
        }
    },
    WARN {
        @Override
        public void log(Logger logger, Object object) {
            logger.warn(object == null ? null : object.toString());
        }

        @Override
        public boolean isEnabled(Logger logger) {
            return logger.isWarnEnabled();
        }
    },
    INFO {
        @Override
        public void log(Logger logger, Object object) {
            logger.info(object == null ? null : object.toString());
        }

        @Override
        public boolean isEnabled(Logger logger) {
            return logger.isInfoEnabled();
        }
    },
    DEBUG {
        @Override
        public void log(Logger logger, Object object) {
            logger.debug(object == null ? null : object.toString());
        }

        @Override
        public boolean isEnabled(Logger logger) {
            return logger.isDebugEnabled();
        }
    },
    TRACE {
        @Override
        public void log(Logger logger, Object object) {
            logger.trace(object == null ? null : object.toString());
        }

        @Override
        public boolean isEnabled(Logger logger) {
            return logger.isTraceEnabled();
        }
    };

    public abstract void log(Logger logger, Object object);

    public abstract boolean isEnabled(Logger logger);
}
