/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.connection.jlan;

import org.alfresco.jlan.debug.DebugConfigSection;
import org.alfresco.jlan.debug.DebugInterface;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.*;
import org.alfresco.jlan.server.auth.acl.DefaultAccessControlManager;
import org.alfresco.jlan.server.config.*;
import org.alfresco.jlan.server.core.DeviceContextException;
import org.alfresco.jlan.server.filesys.*;
import org.alfresco.jlan.smb.server.CIFSConfigSection;
import org.springframework.extensions.config.element.GenericConfigElement;

import java.nio.file.Path;

public class JLANFileServerConfiguration extends ServerConfiguration {
    private static final int DefaultThreadPoolInit  = 25;
    private static final int DefaultThreadPoolMax   = 50;

    private static final int[] DefaultMemoryPoolBufSizes  = { 256, 4096, 16384, 66000 };
    private static final int[] DefaultMemoryPoolInitAlloc = {  20,   20,     5,     5 };
    private static final int[] DefaultMemoryPoolMaxAlloc  = { 100,   50,    50,    50 };

    public JLANFileServerConfiguration(String hostName, String shareRoot, Path localPath, String domain, UserAccountList userAccounts) throws InvalidConfigurationException, DeviceContextException {
        super(hostName);
        setServerName(hostName);

        // DEBUG
        DebugConfigSection debugConfig = new DebugConfigSection(this);
        debugConfig.setDebug("com.mulesoft.connector.smb.internal.connection.jlan.JLANDummyDebug", null);

        // CORE
        CoreServerConfigSection coreConfig = new CoreServerConfigSection(this);
        coreConfig.setMemoryPool(DefaultMemoryPoolBufSizes, DefaultMemoryPoolInitAlloc, DefaultMemoryPoolMaxAlloc);
        coreConfig.setThreadPool(DefaultThreadPoolInit, DefaultThreadPoolMax);
        coreConfig.getThreadPool().setDebug(false);

        // GLOBAL
        GlobalConfigSection globalConfig = new GlobalConfigSection(this);

        // SECURITY
        SecurityConfigSection secConfig = new SecurityConfigSection(this);
        DefaultAccessControlManager accessControlManager = new DefaultAccessControlManager();
        accessControlManager.setDebug(false);
        accessControlManager.initialize(this, new GenericConfigElement("aclManager"));
        secConfig.setAccessControlManager(accessControlManager);
        secConfig.setJCEProvider("cryptix.jce.provider.CryptixCrypto");
        secConfig.setUserAccounts(userAccounts);

        // SHARES
        FilesystemsConfigSection filesysConfig = new FilesystemsConfigSection(this);
        DiskInterface diskInterface = new org.alfresco.jlan.smb.server.disk.JavaFileDiskDriver();
        final GenericConfigElement driverConfig = new GenericConfigElement("driver");
        final GenericConfigElement localPathConfig = new GenericConfigElement("LocalPath");
        localPathConfig.setValue(localPath.toString());
        driverConfig.addChild(localPathConfig);
        DiskDeviceContext diskDeviceContext = (DiskDeviceContext) diskInterface.createContext(shareRoot, driverConfig);
        diskDeviceContext.setShareName(shareRoot);
        diskDeviceContext.setConfigurationParameters(driverConfig);
        diskDeviceContext.enableChangeHandler(false);
        diskDeviceContext.setDiskInformation(new SrvDiskInfo(256000, 64, 512, 230400));// Default to a 8Gb sized disk with 90% free space
        DiskSharedDevice diskDev = new DiskSharedDevice(shareRoot, diskInterface, diskDeviceContext);
        diskDev.setConfiguration(this);
        diskDev.setAccessControlList(secConfig.getGlobalAccessControls());
        diskDeviceContext.startFilesystem(diskDev);
        filesysConfig.addShare(diskDev);

        // SMB
        CIFSConfigSection cifsConfig = new CIFSConfigSection(this);
        cifsConfig.setServerName(hostName);
        cifsConfig.setDomainName(domain);
        cifsConfig.setHostAnnounceInterval(5);
        cifsConfig.setHostAnnouncer(true);
        final CifsAuthenticator authenticator = new LocalAuthenticator() {
            @Override
            public int authenticateUser(ClientInfo client, SrvSession sess, int alg) {
                return AUTH_ALLOW;
            }
        };
        authenticator.setDebug(false);
        authenticator.setAllowGuest(true);
        authenticator.setAccessMode(CifsAuthenticator.USER_MODE);
        final GenericConfigElement authenticatorConfigElement = new GenericConfigElement("authenticator");
        authenticator.initialize(this, authenticatorConfigElement);
        cifsConfig.setAuthenticator(authenticator);
        cifsConfig.setHostAnnounceDebug(false);
        cifsConfig.setNetBIOSDebug(false);
        cifsConfig.setSessionDebugFlags(-1);
        cifsConfig.setTcpipSMB(true);
    }
}
