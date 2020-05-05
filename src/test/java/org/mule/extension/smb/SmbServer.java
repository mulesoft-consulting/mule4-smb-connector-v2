/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.smb;

import org.alfresco.jlan.server.NetworkServer;
import org.alfresco.jlan.server.auth.UserAccount;
import org.alfresco.jlan.server.auth.UserAccountList;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.smb.server.SMBServer;
import org.mule.extension.smb.internal.connection.jlan.JLANFileServerConfiguration;
import org.mule.runtime.api.exception.MuleRuntimeException;

import java.nio.file.Path;

import static java.lang.Thread.sleep;

public class SmbServer {

    public static final String HOSTNAME = "SMBSERVER";
    public static final String SHARE_ROOT = "share";
    public static final String DOMAIN = "WORKGROUP";
    public static final String USERNAME = "mulesoft";
    public static final String PASSWORD = "mulesoft";
    private SMBServer smbServer;
    private ServerConfiguration cfg;

    public SmbServer(Path localPath) {

        try {
            UserAccountList userAccounts = new UserAccountList();
            userAccounts.addUser(new UserAccount(USERNAME, PASSWORD));

            cfg = new JLANFileServerConfiguration(HOSTNAME, SHARE_ROOT, localPath, DOMAIN, userAccounts);

            //NetBIOSNameServer netBIOSNameServer = new NetBIOSNameServer(cfg);
            //cfg.addServer(netBIOSNameServer);
            smbServer = new SMBServer(cfg);
            cfg.addServer(smbServer);
        } catch (Exception e) {
            throw new MuleRuntimeException(e);
        }

    }

    public void start() throws InterruptedException {
        // start servers
        for (int i = 0; i < cfg.numberOfServers(); i++) {
            NetworkServer server = cfg.getServer(i);
            server.startServer();
            // FIXME olamiral: no proud of myself using Thread.sleep
            while (!server.isActive()) {
                Thread.sleep(50);
            }
            System.out.println("SMB server running");
        }
    }

    public void stop() throws InterruptedException {
        // Shutdown servers
        for (int i = 0; i < cfg.numberOfServers(); i++) {
            NetworkServer server = cfg.getServer(i);
            server.shutdownServer(false);
            // FIXME olamiral: no proud of myself using Thread.sleep
            while (!server.hasShutdown()) {
                Thread.sleep(50);
            }
        }
        smbServer = null;
        System.out.println("SMB server shutdown complete");
    }
}
