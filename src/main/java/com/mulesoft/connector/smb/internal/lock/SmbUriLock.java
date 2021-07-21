/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.lock;

import org.mule.extension.file.common.api.lock.UriLock;
import org.mule.runtime.api.lock.LockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

/**
 * A {@link UriLock} which is based on {@link Lock locks} obtained through a
 * {@link #lockFactory}. The lock's keys are generated through the external form
 * of a {@link URL}
 *
 * @since 1.0
 */
public class SmbUriLock implements UriLock {

  private static final Logger logger = LoggerFactory.getLogger(SmbUriLock.class);

  private final URI uri;
  private final LockFactory lockFactory;
  private final AtomicReference<Lock> ownedLock = new AtomicReference<>();

  /**
   * Creates a new instance
   *
   * @param uri        the absolute SMB path that will be used as the lock key
   * @param lockFactory a {@link LockFactory}
   */
  public SmbUriLock(URI uri, LockFactory lockFactory) {
    this.uri = uri;
    this.lockFactory = lockFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean tryLock() {
    Lock lock = getLock();
    if (lock.tryLock()) {
      ownedLock.set(lock);
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isLocked() {
    throw new UnsupportedOperationException("This method is not used by External File Systems.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void release() {
    Lock lock = ownedLock.getAndSet(null);
    if (lock != null) {
      try {
        lock.unlock();
      } catch (Exception e) {
        logger.debug("Failed releasing lock for '{}': {}", uri.getPath(), e.getMessage(), e);
      }
    }
  }

  private Lock getLock() {
    return lockFactory.createLock(uri.toString());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public URI getUri() {
    return this.uri;
  }

}
