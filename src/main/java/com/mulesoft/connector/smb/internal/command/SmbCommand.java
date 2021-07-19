/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.command;

import com.mulesoft.connector.smb.api.SmbFileAttributes;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import com.mulesoft.connector.smb.internal.connection.client.SmbClient;
import org.apache.commons.io.FilenameUtils;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileSystem;
import org.mule.extension.file.common.api.command.ExternalFileCommand;
import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;
import org.mule.extension.file.common.api.exceptions.IllegalPathException;
import org.mule.runtime.core.api.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection.ROOT;
import static com.mulesoft.connector.smb.internal.utils.SmbUtils.normalizePath;
import static java.lang.String.format;
import static org.mule.extension.file.common.api.util.UriUtils.*;

/**
 * Base class for {@link ExternalFileCommand} implementations that target a SMB server
 *
 * @since 1.0
 */
public abstract class SmbCommand extends ExternalFileCommand<SmbFileSystemConnection> {

  private static final Logger logger = LoggerFactory.getLogger(SmbCommand.class);

  protected final SmbClient client;

  protected SmbCommand(SmbFileSystemConnection fileSystem) {
    this(fileSystem, fileSystem.getClient());
  }

  /**
   * Creates a new instance
   *
   * @param fileSystem a {@link SmbFileSystemConnection} used as the connection object
   * @param client a {@link SmbClient}
   */
  protected SmbCommand(SmbFileSystemConnection fileSystem, SmbClient client) {
    super(fileSystem);
    this.client = client;
  }

  /**
   * Similar to {@link #getFile(String)} but throwing an {@link IllegalArgumentException} if the
   * {@code filePath} doesn't exist
   *
   * @param filePath the path to the file you want
   * @return a {@link SmbFileAttributes}
   * @throws IllegalArgumentException if the {@code filePath} doesn't exist
   */
  protected SmbFileAttributes getExistingFile(String filePath) {
    return getFile(filePath, true);
  }

  /**
   * Obtains a {@link SmbFileAttributes} for the given {@code filePath}
   *
   * @param filePath the path to the file you want
   * @return a {@link SmbFileAttributes} or {@code null} if it doesn't exist
   */
  public SmbFileAttributes getFile(String filePath) {
    return getFile(filePath, false);
  }

  public SmbFileAttributes getFile(URI uri) {
    return getFile(uri, false);
  }


  private SmbFileAttributes getFile(String filePath, boolean requireExistence) {
    return getFile(resolvePath(normalizePath(filePath)), requireExistence);
  }

  private SmbFileAttributes getFile(URI uri, boolean requireExistence) {
    SmbFileAttributes attributes;
    try {
      attributes = client.getAttributes(uri);
    } catch (Exception e) {
      throw exception("Found exception trying to obtain path " + uri.getPath(), e);
    }

    if (attributes != null) {
      return attributes;
    } else {
      if (requireExistence) {
        throw pathNotFoundException(uri);
      } else {
        return null;
      }
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean exists(URI uri) {
    return ROOT.equals(uri.toString()) || getFile(uri) != null;
  }

  /**
   * Template method that renames the file at {@code filePath} to {@code newName}.
   * <p>
   * This method performs path resolution and validation and eventually delegates into {@link #doRename(String, String)}, in which
   * the actual renaming implementation is.
   *
   * @param filePath the path of the file to be renamed
   * @param newName the new name
   * @param overwrite whether to overwrite the target file if it already exists
   */
  protected void rename(String filePath, String newName, boolean overwrite) {
    URI sourceUri = resolveExistingPath(filePath);
    URI targetUri = createUri(trimLastFragment(sourceUri).getPath(), newName);

    if (exists(targetUri)) {
      if (!overwrite) {
        throw new FileAlreadyExistsException(format("'%s' cannot be renamed because '%s' already exists", sourceUri.getPath(),
                                                    targetUri.getPath()));
      }

      try {
        fileSystem.delete(targetUri.getPath());
      } catch (Exception e) {
        throw exception(format("Exception was found deleting '%s' as part of renaming '%s'", targetUri.getPath(),
                               sourceUri.getPath()),
                        e);
      }
    }

    try {
      doRename(sourceUri.getPath(), targetUri.getPath());
      logger.debug("{} renamed to {}", filePath, newName);
    } catch (Exception e) {
      throw exception(format("Exception was found renaming '%s' to '%s'", sourceUri.getPath(), newName), e);
    }
  }

  /**
   * Template method which works in tandem with {@link #rename(String, String, boolean)}.
   * <p>
   * Implementations are to perform the actual renaming logic here
   *
   * @param filePath the path of the file to be renamed
   * @param newName the new name
   */
  protected void doRename(String filePath, String newName) {
    client.rename(filePath, newName);
  }

  protected void createDirectory(String directoryPath) {
    final URI uri = createUri(fileSystem.getBasePath(), directoryPath);

    FileAttributes targetFile = getFile(directoryPath);

    if (targetFile != null) {
      throw new FileAlreadyExistsException(format("Directory '%s' already exists", uri.getPath()));
    }

    mkdirs(normalizeUri(uri));
  }

  /**
   * Performs the base logic and delegates into
   * {@link SmbCopyDelegate#doCopy(FileConnectorConfig, FileAttributes, URI, boolean)} to perform the actual
   * copying logic
   *  @param config the config that is parameterizing this operation
   * @param source the path to be copied
   * @param target the path to the target destination
   * @param overwrite whether to overwrite existing target paths
   * @param createParentDirectory whether to create the target's parent directory if it doesn't exist
   */
  protected final void copy(FileConnectorConfig config, String source, String target, boolean overwrite,
                            boolean createParentDirectory, String renameTo, SmbCopyDelegate delegate) {
    FileAttributes sourceFile = getExistingFile(source);

    validateTargetPath(source, target, delegate.getOperation(), createParentDirectory);

    URI targetUri = validateDirectoryTree(sourceFile, target, renameTo, overwrite, delegate.getOperation());

    delegate.doCopy(config, sourceFile, targetUri, overwrite);
    logger.debug("Successfully executed '{}' operation ('{}' to '{}')", delegate.getOperation(), sourceFile, targetUri);
  }

  private void validateTargetPath(String source, String target, String operation, boolean createParentDirectory) {
    URI targetUri = resolvePath(target);
    FileAttributes targetPath = getFile(targetUri.getPath());

    if (targetPath != null) {
      if (targetPath.isRegularFile()) {
        throw new IllegalPathException(format("Cannot %s '%s': target '%s' path exists but it's not a directory",
                                              operation, source, target));
      }
    } else if (!createParentDirectory) {
      throw pathNotFoundException(targetUri);
    }
  }

  private URI getEffectiveTargetUri(String source, String target, String renameTo) {
    String targetFileName = StringUtils.isBlank(renameTo) ? getFileName(source) : renameTo;
    return createUri(target, targetFileName);
  }

  private URI validateDirectoryTree(FileAttributes sourceFile, String target, String renameTo, boolean overwrite,
                                    String operation) {
    URI result = getEffectiveTargetUri(sourceFile.getPath(), target, renameTo);
    if (result.getPath().equals(sourceFile.getPath())) {
      throw new IllegalPathException(format("Cannot %s '%s': source and target paths are the same", operation,
                                            sourceFile.getPath()));
    }

    if (sourceFile.isDirectory() && result.getPath().startsWith(sourceFile.getPath())) {
      throw new IllegalPathException(format("Cannot %s '%s': source path is a directory and target path shares the same directory tree",
                                            operation,
                                            sourceFile.getPath()));
    }

    createParentDirectory(result, overwrite);

    return result;
  }

  private void createParentDirectory(URI targetUri, boolean overwrite) {
    FileAttributes targetPath = getFile(targetUri.getPath());

    if (targetPath != null && !overwrite) {
      throw alreadyExistsException(targetUri);
    }

    mkdirs(getParent(targetUri));
  }


  private String getFileName(String path) {
    // This path needs to be normalized first because if it ends in a separator the method will return an empty String.
    return FilenameUtils.getName(normalizeUri(createUri(path)).getPath());
  }


  /**
   * {@inheritDoc}
   */
  @Override
  protected void doMkDirs(URI directoryUri) {
    client.mkdir(directoryUri);
  }

  /**
   * {@inheritDoc}
   */
  protected URI getBasePath(FileSystem fileSystem) {
    return createUri(fileSystem.getBasePath());
  }

}
