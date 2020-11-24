/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package org.mule.extension.smb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.mule.extension.file.common.api.stream.AbstractNonFinalizableFileInputStream;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;

import java.io.IOException;
import java.io.InputStream;

public class StreamCloserTestMessageProcessor implements Processor {

  @Override
  public CoreEvent process(CoreEvent event) throws MuleException {
    try {
      assertThat(((AbstractNonFinalizableFileInputStream) event.getMessage().getPayload().getValue()).isLocked(), is(true));
      ((InputStream) event.getMessage().getPayload().getValue()).close();
    } catch (IOException e) {
      throw new MuleRuntimeException(e);
    }
    return event;
  }
}
