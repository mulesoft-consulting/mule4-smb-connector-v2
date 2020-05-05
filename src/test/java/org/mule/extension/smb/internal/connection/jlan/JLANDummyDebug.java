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
