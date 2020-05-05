package org.mule.extension.smb.internal;

import static java.lang.String.format;

import java.util.Objects;

import javax.inject.Inject;

import org.mule.extension.file.common.api.FileSystemProvider;
import org.mule.extension.smb.internal.connection.SmbClient;
import org.mule.extension.smb.internal.connection.SmbClientFactory;
import org.mule.extension.smb.internal.connection.SmbFileSystem;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.api.lock.LockFactory;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link FileSystemProvider} which provides instances of
 * {@link SmbFileSystem} from instances of {@link SmbConnector}
 *
 * @since 1.0
 */
@DisplayName("SMB Connection")
public class SmbConnectionProvider extends FileSystemProvider<SmbFileSystem>
		implements PoolingConnectionProvider<SmbFileSystem> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SmbConnectionProvider.class);

	private static final String SMB_ERROR_MESSAGE_MASK = "Could not establish SMB connection (host: '%s', domain: %s, user: %s, share root: '%s'): %s";

	@Inject
	private LockFactory lockFactory;

	/**
	 * The SMB server hostname or ip address
	 */
	@Parameter
	@Placement(order = 1)
	private String host;

	/**
	 * The user domain. Required if the server uses NTLM authentication
	 */
	@Parameter
	@Optional
	@Placement(order = 2)
	private String domain;

	/**
	 * Username. Required if the server uses NTLM authentication.
	 */
	@Parameter
	@Optional
	@Placement(order = 3)
	protected String username;

	/**
	 * Password. Required if the server uses NTLM authentication.
	 */
	@Parameter
	@Optional
	@Password
	@Placement(order = 4)
	private String password;

	/**
	 * The share root
	 */
	@Parameter
	@Optional
	@Summary("The SMB share to be considered as the root of every path" +
			" (relative or absolute) used with this connector")
	@Placement(order = 5)
	private String shareRoot;

	private SmbClientFactory clientFactory = new SmbClientFactory();

	@Override
	public SmbFileSystem connect() throws ConnectionException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(format("Connecting to SMB server (host: '%s', domain: '%s', user: '%s', share Root: '%s')", host,
					domain, username, shareRoot));
		}
		SmbClient client = clientFactory.createInstance(host, shareRoot);
		try {
			client.login(domain, username, password);
		} catch (Exception e) {
			throw new ConnectionException(getErrorMessage(e.getMessage()), e);
		}

		return new SmbFileSystem(client, lockFactory);
	}

	@Override
	public void disconnect(SmbFileSystem fileSystem) {
		fileSystem.disconnect();
	}

	@Override
	public ConnectionValidationResult validate(SmbFileSystem fileSystem) {
		return fileSystem.validateConnection();
	}

	public void setClientFactory(SmbClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWorkingDir() {
		// TODO: verify if it's valid to assume the share root as the working directory
		return this.shareRoot;
	}

	private String getErrorMessage(String message) {
		return format(SMB_ERROR_MESSAGE_MASK, this.host, this.domain, this.username, this.shareRoot, message);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		if (!super.equals(o)) {
			return false;
		}

		SmbConnectionProvider that = (SmbConnectionProvider) o;
		return Objects.equals(host, that.host) && Objects.equals(domain, that.domain)
				&& Objects.equals(username, that.username) && Objects.equals(password, that.password)
				&& Objects.equals(shareRoot, that.shareRoot);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), host, domain, username, password, shareRoot);
	}

	// This validation needs to be done because of the bug explained in MULE-15197
	@Override
	public void onReturn(SmbFileSystem connection) {
		if (!connection.validateConnection().isValid()) {
			LOGGER.debug("Connection is not valid, it is destroyed and not returned to the pool.");
			throw new IllegalStateException("Connection that is being returned to the pool is invalid.");
		}
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setShareRoot(String shareRoot) {
		this.shareRoot = shareRoot;
	}


}
