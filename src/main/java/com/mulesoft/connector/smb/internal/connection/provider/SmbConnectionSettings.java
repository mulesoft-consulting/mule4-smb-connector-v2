/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.connector.smb.internal.connection.provider;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.util.Objects;

/**
 * Groups SMB connection settings
 *
 * @since 1.0
 */
public final class SmbConnectionSettings {

  /**
   * The SMB server hostname or ip address
   */
  @Parameter
  @Placement(order = 1)
  private String host;

  /**
   * The SMB server hostname or ip address
   */
  @Parameter
  @Optional(defaultValue = "445")
  @Placement(order = 2)
  private Integer port;


  /**
   * The user domain. Required if the server uses NTLM authentication
   */
  @Parameter
  @Optional
  @Placement(order = 3)
  private String domain;

  /**
   * Username. Required if the server uses NTLM authentication.
   */
  @Parameter
  @Optional
  @Placement(order = 4)
  protected String username;

  /**
   * Password. Required if the server uses NTLM authentication.
   */
  @Parameter
  @Optional
  @Password
  @Placement(order = 5)
  private String password;

  /**
   * The share root
   */
  @Parameter
  @Optional
  @Summary("The SMB share to be considered as the root of every path" +
      " (relative or absolute) used with this connector")
  @Placement(order = 6)
  private String shareRoot;

  /**
   * DFS enabled
   */
  @Parameter
  @Optional(defaultValue = "true")
  @Summary("Indicates if DFS is enabled. Default: true")
  @Placement(order = 7)
  private boolean dfsEnabled;

  public String getHost() {
    return host;
  }

  public Integer getPort() {
    return port;
  }

  public String getDomain() {
    return domain;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getShareRoot() {
    return shareRoot;
  }

  public boolean getDfsEnabled() {
    return dfsEnabled;
  }

  void setHost(String host) {
    this.host = host;
  }

  void setShareRoot(String shareRoot) {
    this.shareRoot = shareRoot;
  }

  void setDomain(String domain) {
    this.domain = domain;
  }

  void setUsername(String username) {
    this.username = username;
  }

  void setPassword(String password) {
    this.password = password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    SmbConnectionSettings that = (SmbConnectionSettings) o;
    return dfsEnabled == that.dfsEnabled && Objects.equals(host, that.host) && Objects.equals(port, that.port)
        && Objects.equals(domain, that.domain) && Objects.equals(username, that.username)
        && Objects.equals(password, that.password) && Objects.equals(shareRoot, that.shareRoot);
  }

  @Override
  public int hashCode() {
    return Objects.hash(host, port, domain, username, password, shareRoot, dfsEnabled);
  }
}
