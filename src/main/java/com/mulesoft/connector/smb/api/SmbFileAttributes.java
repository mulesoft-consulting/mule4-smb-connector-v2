/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.api;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import org.mule.extension.file.common.api.AbstractFileAttributes;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import jcifs.smb.SmbFile;

/**
 * Metadata about a file in a SMBs server
 *
 * @since 1.0
 */
public class SmbFileAttributes extends AbstractFileAttributes {

    /**
     *
     */
    private static final long serialVersionUID = -2357110369914630644L;

    @Parameter
    private long size;

    @Parameter
    private boolean directory;

    @Parameter
    private boolean regularFile;

    @Parameter
    private boolean exists;

    @Parameter
    private LocalDateTime timestamp;

    @Parameter
    private LocalDateTime createTime;

    @Parameter
    private LocalDateTime lastModified;

    @Parameter
    private LocalDateTime lastAccess;

    @Parameter
    private String absolutePath;

    /**
     * Creates a new instance
     *
     * @param file the file's {@link SmbFile}, from which the attributes will be read
     * @throws Exception
     */
    public SmbFileAttributes(URI uri, SmbFile file) throws Exception {
        super(uri);
        this.exists = file.exists();
        this.absolutePath = file.getPath();
        if (this.exists) {
            this.size = file.length();
            this.directory = file.isDirectory();
            this.regularFile = file.isFile();
            this.createTime = localDateTimeFromEpoch(file.createTime());
            this.lastModified = localDateTimeFromEpoch(file.lastModified());
            this.lastAccess = localDateTimeFromEpoch(file.lastAccess());
            this.timestamp = localDateTimeFromEpoch(file.getLastModified());
        }
    }

    private LocalDateTime localDateTimeFromEpoch(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public boolean isDirectory() {
        return this.directory;
    }

    @Override
    public boolean isRegularFile() {
        return this.regularFile;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    /**
     * @return The last time the file was modified
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public LocalDateTime getLastAccess() {
        return lastAccess;
    }

    public boolean exists() { return exists; }

    @Override
    public String getPath() {
        return this.absolutePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmbFileAttributes that = (SmbFileAttributes) o;
        return size == that.size && directory == that.directory && regularFile == that.regularFile && exists == that.exists && Objects.equals(timestamp, that.timestamp) && Objects.equals(createTime, that.createTime) && Objects.equals(lastModified, that.lastModified) && Objects.equals(lastAccess, that.lastAccess) && Objects.equals(absolutePath, that.absolutePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, directory, regularFile, exists, timestamp, createTime, lastModified, lastAccess, absolutePath);
    }

}
