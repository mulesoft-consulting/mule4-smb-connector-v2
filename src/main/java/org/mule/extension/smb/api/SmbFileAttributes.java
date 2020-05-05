package org.mule.extension.smb.api;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

}
