/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.command;

import static java.lang.String.format;
import static org.mule.extension.file.common.api.util.UriUtils.createUri;
import static org.mule.extension.file.common.api.util.UriUtils.normalizeUri;
import static org.mule.extension.file.common.api.util.UriUtils.trimLastFragment;
import static com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection.ROOT;
import static com.mulesoft.connector.smb.internal.utils.SmbUtils.normalizePath;

import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.FileSystem;
import org.mule.extension.file.common.api.command.ExternalFileCommand;
import org.mule.extension.file.common.api.exceptions.FileAlreadyExistsException;
import com.mulesoft.connector.smb.api.SmbFileAttributes;
import com.mulesoft.connector.smb.internal.utils.SmbUtils;
import com.mulesoft.connector.smb.internal.connection.SmbClient;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;

import java.net.URI;

import org.apache.commons.io.FilenameUtils;
import org.mule.runtime.core.api.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for {@link ExternalFileCommand} implementations that target a SMB server
 *
 * @since 1.0
 */
public abstract class SmbCommand extends ExternalFileCommand<SmbFileSystemConnection> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SmbCommand.class);

  protected final SmbClient client;

  public SmbCommand(SmbFileSystemConnection fileSystem) {
    this(fileSystem, fileSystem.getClient());
  }

  /**
   * Creates a new instance
   *
   * @param fileSystem a {@link SmbFileSystemConnection} used as the connection object
   * @param client a {@link SmbClient}
   */
  public SmbCommand(SmbFileSystemConnection fileSystem, SmbClient client) {
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

  @Override
  protected URI resolvePath(String filePath) {
    URI result = null;
    if (filePath != null && filePath.startsWith("smb://")) {
      result = URI.create(SmbUtils.urlEncodePathFragments(filePath));
    } else {
      result = super.resolvePath(filePath);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean exists(URI uri) {
    return uri != null && (ROOT.equals(uri.toString()) || getFile(uri) != null);
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
      doRename(sourceUri.toString(), targetUri.toString());
      LOGGER.debug("{} renamed to {}", filePath, newName);
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
   * @throws Exception if anything goes wrong
   */
  protected void doRename(String filePath, String newName) throws Exception {
    client.rename(filePath, newName, false);
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
    URI targetUri = resolvePath(target);
    FileAttributes targetFile = getFile(targetUri.getPath());
    String targetFileName = StringUtils.isBlank(renameTo) ? getFileName(source) : renameTo;

    if (targetFile != null) {
      if (targetFile.isDirectory()) {
        if (sourceFile.isDirectory() && sourceFile.getName().equals(targetFile.getName()) && !overwrite) {
          throw alreadyExistsException(targetUri);
        } else {
          targetUri = createUri(targetUri.getPath(), targetFileName);
        }
      } else if (!overwrite) {
        throw alreadyExistsException(targetUri);
      }
    } else {
      if (createParentDirectory) {
        mkdirs(targetUri);
        targetUri = createUri(targetUri.getPath(), targetFileName);
      } else {
        throw pathNotFoundException(targetUri);
      }
    }

    delegate.doCopy(config, sourceFile, targetUri, overwrite);
    LOGGER.debug("Copied '{}' to '{}'", sourceFile, targetUri);
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

    // SMB Client implement this algorithm (supports mkdirs)
    /*
    Stack<URI> fragments = new Stack<>();
    String[] subPaths = directoryUri.getPath().split("/");
    // This uri needs to be normalized so that if it has a trailing separator it is erased.
    URI subUri = normalizeUri(directoryUri);
    for (int i = subPaths.length - 1; i > 0; i--) {
    	if (exists(subUri)) {
    		break;
    	}
    	fragments.push(subUri);
    	subUri = trimLastFragment(subUri);
    }
    
    while (!fragments.isEmpty()) {
    	URI fragment = fragments.pop();
    	client.mkdir(fragment.getPath());
    }
    */
  }

  /**
   * {@inheritDoc}
   */
  protected URI getBasePath(FileSystem fileSystem) {
    return createUri(fileSystem.getBasePath());
  }

}
