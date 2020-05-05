package org.mule.extension.smb.internal.connection;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import jcifs.CIFSContext;
import jcifs.Credentials;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.mule.extension.file.common.api.FileWriteMode;
import org.mule.extension.file.common.api.util.UriUtils;
import org.mule.extension.smb.api.SmbFileAttributes;
import org.mule.extension.smb.internal.utils.SmbUtils;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.core.api.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

import static org.mule.extension.smb.internal.utils.SmbUtils.normalizePath;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;

/**
 * SmbClient
 *
 * @since 1.0
 */
public class SmbClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmbClient.class);
    private static final CharSequence JCIFS_SMB_PKG = "jcifs.smb";

    private String host;
    private String shareRoot;

    private CIFSContext context;

    private SmbFileSystem owner;

    public SmbClient(String host, String shareRoot) {
        this.host = host;
        this.shareRoot = shareRoot;
        //this.registerSmbUrlHandler();
    }

    /*
    private void registerSmbUrlHandler() {
        String urlHandlersPackagePrefixList = System.getProperty("java.protocol.handler.pkgs","");

        if (!urlHandlersPackagePrefixList.contains(JCIFS_SMB_PKG)) {
            urlHandlersPackagePrefixList += (StringUtils.isEmpty(urlHandlersPackagePrefixList) ? "" : "|") + JCIFS_SMB_PKG;
            System.setProperty("java.protocol.handler.pkgs", urlHandlersPackagePrefixList);
        }
    }
     */

    // FIXME: decouple operations from helper methods
    // FIXME: check if some of the helper method are implemented somewhere else

    /* CONNECTION */

    public void login(String domain, String username, String password) throws Exception {
        if (this.context == null) {
            this.context = SingletonContext.getInstance();
            if (!StringUtils.isBlank(domain) && !StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
                Credentials credentials = new NtlmPasswordAuthenticator(domain, username, password);
                this.context = this.context.withCredentials(credentials);
            } else {
                this.context = this.context.withGuestCrendentials();
            }
        }
        this.connect();
    }

    private void connect() throws Exception {
        SmbFile shareRoot = this.getFile("");
        if (shareRoot == null || !shareRoot.exists()) {
            throw exception("Connection failed: could not access path " + this.getShareRootURL(), null);
        }
    }


    public boolean isConnected() {
        // TODO: check how to check if client is connected or not
        // Assume always true, but need to confirm if this can affect
        // the connector's behavior
        return true;
    }

    public void disconnect() {
        if (context != null) {
            try {
                context.close();
            } catch (Exception e) {
                LOGGER.warn("Could not close SMB context", e);
            }
        }
    }


    /* OPERATIONS */

    public void mkdir(String dirPath) {
        SmbFile dir = null;
        try {
            dir = getFile(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            throw exception("Could not create directory: " + e.getMessage(), e);
        } finally {
            close(dir);
        }
    }


    public List<SmbFileAttributes> list(String directory) {
        SmbFile dir = null;
        try {
            dir = this.getFile(directory != null && !directory.endsWith("/") ? directory + "/" : directory);
            List<SmbFileAttributes> result = new Vector<SmbFileAttributes>();

            for (SmbFile file : dir.listFiles()) {
                URI uri = file.getURL().toURI();
                if (uri.toString().endsWith("/")) {
                    uri = new URI(uri.toString().replaceAll("/$", ""));
                }
                result.add(new SmbFileAttributes(uri, file));
            }

            return result;
        } catch (Exception e) {
            close(dir);
            throw exception("Could not list files in " + directory.toString(), e);
        }
    }

    public void write(String target, InputStream inputStream, FileWriteMode mode) {
        if (inputStream == null) {
            throw exception("Cannot write to file: inputStream is null");
        }

        OutputStream out = null;
        try {
            out = this.getOutputStream(target, mode);
            IOUtils.copyLarge(inputStream, out);
        } catch (Exception e) {
            throw exception("Cannot write to file: " + e.getMessage(), e);
        } finally {
            close(out);
        }
    }

    public OutputStream getOutputStream(String target, FileWriteMode mode) {
        if (target == null) {
            throw exception("Cannot write to file: target is null or empty");
        }

        SmbFile file = null;

        try {
            file = getFile(target);

            if (!file.exists()) {
                file.createNewFile();
            }

            return file.openOutputStream(FileWriteMode.APPEND.equals(mode));
        } catch (Exception e) {
            throw exception("Cannot write to file: " + e.getMessage(), e);
        } finally {
            close(file);
        }
    }


    public InputStream read(String filePath) {
        if (filePath == null) {
            throw exception("Cannot read from file: filePath is null");
        }

        SmbFile file = null;
        try {
            file = getFile(filePath);
            return file.getInputStream();
        } catch (Exception e) {
            throw exception("Cannot read from file: " + e.getMessage(), e);
        } finally {
            close(file);
        }
    }

    public void rename(String sourcePath, String newName, boolean overwrite) {

        if (sourcePath == null) {
            throw exception("Cannot rename sourcePath: sourcePath is null.");
        }

        if (newName == null) {
            throw exception("Cannot rename sourcePath: newName is null.");
        }

        SmbFile source = null;
        SmbFile target = null;

        try {
            source = getFile(sourcePath);
            target = getFile(newName);
            if (target.exists()) {
                if (overwrite && source.exists() && source.isDirectory()) {
                    // FIXME olamiral: replace with overwrite does not work when renaming dir.
                    // Force delete dir before rename only if source is directory;
                    target.delete();
                }
            }
            source.renameTo(target, overwrite);
        } catch (Exception e) {
            throw exception("Cannot rename sourcePath: " + e.getMessage(), e);
        } finally {
            close(source);
        }
    }

    public void copy(String sourcePath, String targetPath) {
        copyOrMove(sourcePath, targetPath, FileCopyMode.COPY);
    }

    public void move(String sourcePath, String targetPath) {
        copyOrMove(sourcePath, targetPath, FileCopyMode.MOVE);
    }

    private void copyOrMove(String sourcePath, String targetPath, FileCopyMode mode) {
        if (sourcePath == null) {
            throw this.exception("Cannot " + mode.label() + " sourcePath to targetDir: sourcePath is null.");
        }

        if (targetPath == null) {
            throw exception("Cannot " + mode.label() + " sourcePath to targetDir: targetDir is null.");
        }

        SmbFile source = null;
        SmbFile target = null;
        try {
            source = getFile(sourcePath);
            target = getFile(targetPath);

            source.copyTo(target);

            if (getFile(targetPath).exists() && FileCopyMode.MOVE.equals(mode)) {
                source.delete();
            }
        } catch (Exception e) {
            throw exception("Could not " + mode.label() + "sourcePath to targetDir: " + e.getMessage(), e);
        } finally {
            close(target);
            close(source);
        }
    }

    public void delete(String path) {
        SmbFile file = null;

        if (path == null) {
            throw exception("Cannot delete path: path is null.");
        }

        try {
            file = getFile(path);
            file.delete();
        } catch (Exception e) {
            throw exception("Cannot delete path: " + e.getMessage(), e);
        } finally {
            close(file);
        }
    }

    /* HELPER Methods */

    public SmbFileAttributes getAttributes(URI uri) {
        SmbFileAttributes result = null;

        SmbFile file = null;
        try {
            file = this.getFile(normalizePath(uri.toString()));
            if (file.exists()) {
                result = new SmbFileAttributes(uri, file);
            }
        } catch (Exception e) {
            if (uri == null) {
                throw exception("Error getting file attributes: path is null", null);
            } else {
                throw exception("Error getting file attributes for path " + uri.getPath(), e);
            }
        } finally {
            close(file);
        }
        return result;
    }

    private SmbFile getFile(String path) throws Exception {
        return new SmbFile(getAbsolutePath(path), this.context);
    }


    /*
   private SmbFile getFile(String path) {
        try {
            return new SmbFile(SmbUtils.normalizeURL(this.host, this.shareRoot, path)
                    , this.context);
        } catch (Exception e) {
            throw exception("Could not get path '" + path + '"', e);
        }
    }
    */

    private void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                //Does nothing
            }
        }
    }

    private RuntimeException exception(String message) {
        return this.exception(message, null);
    }

    private RuntimeException exception(String message, Exception cause) {

        if (cause == null) {
            return new MuleRuntimeException(createStaticMessage(message));
        }

        if (cause instanceof SmbException || cause.getCause() instanceof IOException) {
            return exception(message, new ConnectionException(cause, owner));
        }

        return new MuleRuntimeException(createStaticMessage(message), cause);
    }

    public URI resolve(URI uri) {
        try {
            return SmbUtils.resolve(this.host, this.shareRoot, uri);
        } catch (Exception e) {
            throw exception("Could not resolve URI: " + e.getMessage(), e);
        }
    }

    public URI resolvePath(String filePath) {
        URI result = null;

        if (filePath != null) {
            String actualFilePath = filePath;
            if (!actualFilePath.startsWith("/") && !actualFilePath.startsWith("smb://")) {
                actualFilePath = "/" + actualFilePath;
            }
            result = resolve(UriUtils.createUri(normalizePath(actualFilePath)));

        }
        return result;
    }

    private static String getPath(String basePath, String name) {
        String result = name;

        if (basePath != null && !basePath.isEmpty()) {
            String separator = basePath.endsWith("/") ? "" : "/";
            result = basePath + separator + name.replaceFirst("^/", "");
        }

        return result;
    }

    private String getAbsolutePath(String path) throws Exception {
        String result = path;

        if (result != null) {

            result = URLDecoder.decode(path, StandardCharsets.UTF_8.name());

            if (!result.startsWith("smb://")) {
                result = getShareRootURL() + result.replaceFirst("^/", "");
            }
        }

        return result;
    }

    public String getShareRootURL() {
        return "smb://" + this.host + "/" + this.shareRoot + "/";
    }

    /* Getters and Setters */

    private void setOwner(SmbFileSystem owner) {
        this.owner = owner;
    }

    private String getHost() {
        return this.host;
    }

    public String getShareRoot() throws Exception {
        return this.shareRoot;
    }

    public boolean pathIsShareRoot(String path) {
        return this.getShareRootURL().equals(path)
                || this.getShareRootURL().equals(path != null ? path + "/" : null);
    }
}
