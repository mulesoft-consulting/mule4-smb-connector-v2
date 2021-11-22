/**
 * (c) 2003-2021 MuleSoft, Inc. The software in this package is
 * published under the terms of the Commercial Free Software license V.1, a copy of which
 * has been included with this distribution in the LICENSE.md file.
 */
package com.mulesoft.connector.smb.internal.lifecycle;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import static java.lang.String.format;

public class SmbServerContainerLifecycleManger {

  private static final Logger LOGGER = LoggerFactory.getLogger(SmbServerContainerLifecycleManger.class);
  private static final String SMB_SERVER_NAME = "samba";

  public static Container getContainerByName(String containerName) throws Exception {
    LOGGER.info("Getting containers...");
    final DockerClient docker = DefaultDockerClient.fromEnv().build();
    final List<Container> containers = docker.listContainers(DockerClient.ListContainersParam.allContainers());

    LOGGER.info("Found " + containers.size() + " containers. Getting by name...");

    for (Container container : containers) {
      LOGGER.info("Container: " + container.image());
      if (container.image().contains(containerName)) {
        return container;
      }
    }

    throw new Exception(format("No container found for name {%s}", containerName));
  }


  public static String stopServerContainer(String containerName, int delay) throws Exception {
    final DockerClient docker = DefaultDockerClient.fromEnv().build();
    Container smbServerContainer = getContainerByName(SMB_SERVER_NAME);
    docker.stopContainer(smbServerContainer.id(), delay);
    LOGGER.info(String.format("STOPPING DOCKER CONTAINER %s", smbServerContainer.id()));
    return smbServerContainer.id();
  }

  public static void startServerContainer(String containerId) throws Exception {
    final DockerClient docker = DefaultDockerClient.fromEnv().build();
    Container smbServerContainer = getContainerByName(SMB_SERVER_NAME);
    LOGGER.info(String.format("STARTING DOCKER CONTAINER %s", containerId));
    docker.startContainer(smbServerContainer.id());
  }

}
