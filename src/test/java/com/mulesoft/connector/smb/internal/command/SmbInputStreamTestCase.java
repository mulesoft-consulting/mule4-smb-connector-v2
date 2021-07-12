/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.command;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mule.extension.file.common.api.lock.UriLock;

import java.io.ByteArrayInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SmbInputStreamTestCase {

  public static final String STREAM_CONTENT = "My stream content";

  @Mock
  private UriLock uriLock;

  @Mock
  private SmbInputStream.SmbFileInputStreamSupplier streamSupplier;

  @Before
  public void setUp() {
    when(uriLock.isLocked()).thenReturn(true);
    doAnswer(invocation -> {
      when(uriLock.isLocked()).thenReturn(false);
      return null;
    }).when(uriLock).release();

    when(streamSupplier.get()).thenReturn(new ByteArrayInputStream(STREAM_CONTENT.getBytes(UTF_8)));
  }

  @Test
  public void readLockReleasedOnContentConsumed() throws Exception {
    SmbInputStream inputStream = new SmbInputStream(streamSupplier, uriLock);

    verifyZeroInteractions(uriLock);
    assertThat(inputStream.isLocked(), is(true));
    verify(uriLock).isLocked();

    org.apache.commons.io.IOUtils.toString(inputStream, UTF_8);

    verify(uriLock, times(1)).release();
    assertThat(inputStream.isLocked(), is(false));
    verify(streamSupplier).releaseConnectionUsedForContentInputStream();
  }

  @Test
  public void readLockReleasedOnEarlyClose() throws Exception {
    SmbInputStream inputStream = new SmbInputStream(streamSupplier, uriLock);

    verifyZeroInteractions(uriLock);
    assertThat(inputStream.isLocked(), is(true));
    verify(uriLock).isLocked();

    inputStream.close();

    verify(uriLock, times(1)).release();
    assertThat(inputStream.isLocked(), is(false));
  }

}
