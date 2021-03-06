/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.command;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb2.SMBApiException;
import com.mulesoft.connector.smb.api.SmbFileAttributes;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import com.mulesoft.connector.smb.internal.config.SmbConfiguration;
import org.mule.extension.file.common.api.AbstractConnectedFileInputStreamSupplier;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.lock.UriLock;
import org.mule.extension.file.common.api.stream.AbstractNonFinalizableFileInputStream;
import org.mule.extension.file.common.api.stream.LazyStreamSupplier;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.connector.ConnectionManager;

import java.io.IOException;
import java.io.InputStream;


/**
 * Implementation of {@link AbstractNonFinalizableFileInputStream} for SMB connections
 *
 * @since 1.0
 */
public class SmbInputStream extends AbstractNonFinalizableFileInputStream {

  protected static ConnectionManager getConnectionManager(SmbConfiguration config) {
    return config.getConnectionManager();
  }

  /**
   * Establishes the underlying connection and returns a new instance of this class.
   * <p>
   * Instances returned by this method <b>MUST</b> be closed or fully consumed.
   *
   * @param config the config which is parameterizing this operation
   * @param attributes a {@link FileAttributes} referencing the file which contents are to be fetched
   * @param lock the {@link UriLock} to be used
   * @param timeBetweenSizeCheck time in milliseconds to wait between size checks to decide if a file is ready to be read
   * @return a new {@link SmbFileAttributes}
   */
  public static SmbInputStream newInstance(SmbConfiguration config, SmbFileAttributes attributes, UriLock lock,
                                           Long timeBetweenSizeCheck) {
    SmbFileInputStreamSupplier fileInputStreamSupplier =
        new SmbFileInputStreamSupplier(attributes, getConnectionManager(config), timeBetweenSizeCheck, config);
    return new SmbInputStream(fileInputStreamSupplier, lock);
  }

  /**
   * Using the given connection ,returns a new instance of this class.
   * <p>
   * Instances returned by this method <b>MUST</b> be closed or fully consumed.
   *
   * @param fileSystem            the {@link SmbFileSystemConnection} to be used to connect to the SMB server
   * @param attributes            a {@link FileAttributes} referencing the file which contents are to be fetched
   * @param lock                  the {@link UriLock} to be used
   * @param timeBetweenSizeCheck  the time to be waited between size checks if configured.
   * @return a new {@link SmbInputStream}
   */
  public static SmbInputStream newInstance(SmbFileSystemConnection fileSystem, SmbFileAttributes attributes, UriLock lock,
                                           Long timeBetweenSizeCheck) {
    SmbFileInputStreamSupplier fileInputStreamSupplier =
        new SmbFileInputStreamSupplier(attributes, timeBetweenSizeCheck, fileSystem);
    return new SmbInputStream(fileInputStreamSupplier, lock);
  }

  private final SmbFileInputStreamSupplier smbFileInputStreamSupplier;

  protected SmbInputStream(SmbFileInputStreamSupplier smbFileInputStreamSupplier, UriLock lock) {
    super(new LazyStreamSupplier(smbFileInputStreamSupplier), lock);
    this.smbFileInputStreamSupplier = smbFileInputStreamSupplier;
  }

  @Override
  protected void doClose() throws IOException {
    try {
      super.doClose();
    } finally {
      smbFileInputStreamSupplier.releaseConnectionUsedForContentInputStream();
    }
  }

  protected static class SmbFileInputStreamSupplier extends AbstractConnectedFileInputStreamSupplier<SmbFileSystemConnection> {

    private SmbFileInputStreamSupplier(SmbFileAttributes attributes, ConnectionManager connectionManager,
                                       Long timeBetweenSizeCheck, SmbConfiguration config) {
      super(attributes, connectionManager, timeBetweenSizeCheck, config);
    }

    private SmbFileInputStreamSupplier(SmbFileAttributes attributes, Long timeBetweenSizeCheck,
                                       SmbFileSystemConnection fileSystem) {
      super(attributes, timeBetweenSizeCheck, fileSystem);
    }

    @Override
    protected FileAttributes getUpdatedAttributes(SmbFileSystemConnection fileSystem) {
      return fileSystem.readFileAttributes(attributes.getPath());
    }

    @Override
    protected InputStream getContentInputStream(SmbFileSystemConnection fileSystem) {
      return fileSystem.retrieveFileContent(attributes);
    }

    @Override
    protected boolean fileWasDeleted(MuleRuntimeException e) {
      return e.getCause() instanceof SMBApiException
          && NtStatus.STATUS_OBJECT_NAME_NOT_FOUND.equals(((SMBApiException) e.getCause()).getStatus());
    }
  }
}
