/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.command;

import static org.mule.extension.file.common.api.util.UriUtils.createUri;
import static org.slf4j.LoggerFactory.getLogger;
import org.mule.extension.file.common.api.FileAttributes;
import org.mule.extension.file.common.api.FileConnectorConfig;
import org.mule.extension.file.common.api.command.ListCommand;
import com.mulesoft.connector.smb.api.SmbFileAttributes;
import com.mulesoft.connector.smb.internal.connection.SmbClient;
import com.mulesoft.connector.smb.internal.connection.SmbFileSystemConnection;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.slf4j.Logger;

/**
 * A {@link SmbCommand} which implements the {@link ListCommand} contract
 *
 * @since 1.0
 */
public final class SmbListCommand extends SmbCommand implements ListCommand<SmbFileAttributes> {

  private static final Logger LOGGER = getLogger(SmbListCommand.class);
  private final SmbReadCommand smbReadCommand;

  public SmbListCommand(SmbFileSystemConnection fileSystem, SmbClient client, SmbReadCommand smbReadCommand) {
    super(fileSystem, client);
    this.smbReadCommand = smbReadCommand;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Deprecated
  public List<Result<InputStream, SmbFileAttributes>> list(FileConnectorConfig config,
                                                           String directoryPath,
                                                           boolean recursive,
                                                           Predicate<SmbFileAttributes> matcher) {

    return list(config, directoryPath, recursive, matcher, null);
  }

  @Deprecated
  public List<Result<InputStream, SmbFileAttributes>> list(FileConnectorConfig config,
                                                           String directoryPath,
                                                           boolean recursive,
                                                           Predicate<SmbFileAttributes> matcher,
                                                           Long timeBetweenSizeCheck,
                                                           TimeUnit timeBetweenSizeCheckUnit) {

    return list(config, directoryPath, recursive, matcher,
                config.getTimeBetweenSizeCheckInMillis(timeBetweenSizeCheck, timeBetweenSizeCheckUnit).orElse(null));
  }

  public List<Result<InputStream, SmbFileAttributes>> list(FileConnectorConfig config,
                                                           String directoryPath,
                                                           boolean recursive,
                                                           Predicate<SmbFileAttributes> matcher,
                                                           Long timeBetweenSizeCheck) {

    FileAttributes directoryAttributes = getExistingFile(directoryPath);
    URI uri = createUri(directoryAttributes.getPath(), "");

    if (!directoryAttributes.isDirectory()) {
      throw cannotListFileException(uri);
    }

    List<Result<InputStream, SmbFileAttributes>> accumulator = new LinkedList<>();
    doList(config, directoryAttributes.getPath(), accumulator, recursive, matcher, timeBetweenSizeCheck);

    return accumulator;
  }

  private void doList(FileConnectorConfig config,
                      String path,
                      List<Result<InputStream, SmbFileAttributes>> accumulator,
                      boolean recursive,
                      Predicate<SmbFileAttributes> matcher,
                      Long timeBetweenSizeCheck) {

    LOGGER.debug("Listing directory {}", path);
    for (SmbFileAttributes file : client.list(path)) {

      if (isVirtualDirectory(file.getName())) {
        continue;
      }
      if (file.isDirectory()) {
        if (matcher.test(file)) {
          accumulator.add(Result.<InputStream, SmbFileAttributes>builder().output(null).attributes(file).build());
        }
        if (recursive) {
          doList(config, file.getPath(), accumulator, recursive, matcher, timeBetweenSizeCheck);
        }
      } else {
        if (matcher.test(file)) {
          accumulator.add(smbReadCommand.read(config, file, false, timeBetweenSizeCheck));
        }
      }
    }
  }
}
