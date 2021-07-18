/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection;

import com.mulesoft.connector.smb.api.SmbFileAttributes;
import com.mulesoft.connector.smb.internal.command.*;
import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import com.mulesoft.connector.smb.internal.error.exception.SmbConnectionException;
import com.mulesoft.connector.smb.internal.lock.SmbUriLock;
import com.mulesoft.connector.smb.internal.utils.SmbUtils;
import org.mule.extension.file.common.api.AbstractExternalFileSystem;
import org.mule.extension.file.common.api.AbstractFileSystem;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.command.*;
import org.mule.extension.file.common.api.lock.UriLock;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.lock.LockFactory;

import java.io.InputStream;
import java.net.URI;

import static org.mule.extension.file.common.api.exceptions.FileError.DISCONNECTED;
import static org.mule.runtime.api.connection.ConnectionValidationResult.failure;
import static org.mule.runtime.api.connection.ConnectionValidationResult.success;

/**
 * Implementation of {@link AbstractFileSystem} for files residing on a SMB server
 *
 * @since 1.0
 */
public class SmbFileSystemConnection extends AbstractExternalFileSystem {

  public static final String ROOT = "/";

  protected final SmbClient client;
  protected final CopyCommand copyCommand;
  protected final CreateDirectoryCommand createDirectoryCommand;
  protected final DeleteCommand deleteCommand;
  protected final SmbListCommand listCommand;
  protected final MoveCommand moveCommand;
  protected final SmbReadCommand readCommand;
  protected final RenameCommand renameCommand;
  protected final WriteCommand writeCommand;
  private final LockFactory lockFactory;

  public SmbFileSystemConnection(SmbClient client, LockFactory lockFactory) {
    super("");
    this.client = client;
    this.lockFactory = lockFactory;

    copyCommand = new SmbCopyCommand(this, client);
    createDirectoryCommand = new SmbCreateDirectoryCommand(this, client);
    deleteCommand = new SmbDeleteCommand(this, client);
    moveCommand = new SmbMoveCommand(this, client);
    readCommand = new SmbReadCommand(this, client);
    listCommand = new SmbListCommand(this, client, readCommand);
    renameCommand = new SmbRenameCommand(this, client);
    writeCommand = new SmbWriteCommand(this, client);
  }

  public void disconnect() {
    client.disconnect();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void changeToBaseDir() {
    // Does not apply to SMB
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getBasePath() {
    return SmbUtils.normalizePath(super.getBasePath());
  }

  public InputStream retrieveFileContent(FileAttributes filePayload) {
    return client.read(filePayload.getPath());
  }

  public SmbFileAttributes readFileAttributes(String filePath) {
    return getReadCommand().readAttributes(filePath);
  }

  protected boolean isConnected() {
    return client.isConnected();
  }

  /**
   * {@inheritDoc}
   */
  protected UriLock createLock(URI uri) {
    return new SmbUriLock(uri, lockFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CopyCommand getCopyCommand() {
    return copyCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CreateDirectoryCommand getCreateDirectoryCommand() {
    return createDirectoryCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DeleteCommand getDeleteCommand() {
    return deleteCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SmbListCommand getListCommand() {
    return listCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MoveCommand getMoveCommand() {
    return moveCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public SmbReadCommand getReadCommand() {
    return readCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RenameCommand getRenameCommand() {
    return renameCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WriteCommand getWriteCommand() {
    return writeCommand;
  }

  /**
   * Validates the underlying connection to the remote server
   *
   * @return a {@link ConnectionValidationResult}
   */
  public ConnectionValidationResult validateConnection() {
    if (!isConnected()) {
      return failure("Connection is stale", new SmbConnectionException("Connection is stale", DISCONNECTED));
    }
    return success();
  }

  public SmbClient getClient() {
    return client;
  }


}
