/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package org.mule.extension.smb.internal.connection.jlan;

import org.alfresco.jlan.debug.DebugInterface;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.springframework.extensions.config.ConfigElement;

public class JLANDummyDebug implements DebugInterface {

    @Override
    public void close() {}

    @Override
    public void debugPrint(String str) {}

    @Override
    public void debugPrint(String str, int level) {}

    @Override
    public void debugPrintln(String str) {}

    @Override
    public void debugPrintln(String str, int level) {}

    @Override
    public void debugPrintln(Exception ex, int level) {}

    @Override
    public void initialize(ConfigElement params, ServerConfiguration config) {}

    @Override
    public int getLogLevel() {return 0;}
}
