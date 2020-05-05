package org.mule.extension.smb.internal;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
import org.mule.extension.file.common.api.AbstractConnectedFileInputStreamSupplier;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.lock.UriLock;
import org.mule.extension.file.common.api.stream.AbstractNonFinalizableFileInputStream;
import org.mule.extension.file.common.api.stream.LazyStreamSupplier;
import org.mule.extension.smb.api.SmbFileAttributes;
import org.mule.extension.smb.internal.connection.SmbFileSystem;
import org.mule.runtime.api.connection.ConnectionException;
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

  protected static ConnectionManager getConnectionManager(SmbConnector config) throws ConnectionException {
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
   * @throws ConnectionException if a connection could not be established
   */
  public static SmbInputStream newInstance(SmbConnector config, SmbFileAttributes attributes, UriLock lock,
                                            Long timeBetweenSizeCheck)
      throws ConnectionException {
    SmbFileInputStreamSupplier fileInputStreamSupplier =
        new SmbFileInputStreamSupplier(attributes, getConnectionManager(config), timeBetweenSizeCheck, config);
    return new SmbInputStream(fileInputStreamSupplier, lock);
  }

  /**
   * Using the given connection ,returns a new instance of this class.
   * <p>
   * Instances returned by this method <b>MUST</b> be closed or fully consumed.
   *
   * @param fileSystem            the {@link SmbFileSystem} to be used to connect to the SMB server
   * @param attributes            a {@link FileAttributes} referencing the file which contents are to be fetched
   * @param lock                  the {@link UriLock} to be used
   * @param timeBetweenSizeCheck  the time to be waited between size checks if configured.
   * @return a new {@link SmbInputStream}
   */
  public static SmbInputStream newInstance(SmbFileSystem fileSystem, SmbFileAttributes attributes, UriLock lock,
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

  protected static class SmbFileInputStreamSupplier extends AbstractConnectedFileInputStreamSupplier<SmbFileSystem> {

    private static final String FILE_NOT_FOUND_EXCEPTION = "FileNotFoundException";

    private SmbFileInputStreamSupplier(SmbFileAttributes attributes, ConnectionManager connectionManager,
                                        Long timeBetweenSizeCheck, SmbConnector config) {
      super(attributes, connectionManager, timeBetweenSizeCheck, config);
    }

    private SmbFileInputStreamSupplier(SmbFileAttributes attributes, Long timeBetweenSizeCheck, SmbFileSystem fileSystem) {
      super(attributes, timeBetweenSizeCheck, fileSystem);
    }

    @Override
    protected FileAttributes getUpdatedAttributes(SmbFileSystem fileSystem) {
      return fileSystem.readFileAttributes(attributes.getPath());
    }

    @Override
    protected InputStream getContentInputStream(SmbFileSystem fileSystem) {
      return fileSystem.retrieveFileContent(attributes);
    }

    @Override
    protected boolean fileWasDeleted(MuleRuntimeException e) {
      return e.getCause().getMessage().contains(FILE_NOT_FOUND_EXCEPTION);
    }
  }
}
