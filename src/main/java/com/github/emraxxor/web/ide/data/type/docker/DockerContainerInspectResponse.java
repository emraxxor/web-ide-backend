package com.github.emraxxor.web.ide.data.type.docker;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dockerjava.api.command.GraphDriver;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.command.InspectContainerResponse.Mount;
import com.github.dockerjava.api.command.InspectContainerResponse.Node;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.NetworkSettings;
import com.github.dockerjava.api.model.VolumeBinds;
import com.github.dockerjava.api.model.VolumesRW;

import lombok.Data;

/**
 * 
 * @author attila
 *
 */
@Data
public class DockerContainerInspectResponse {

    private String[] args;
    
    private String config;

    private String created;
    
    private String driver;

    private String execDriver;

    private HostConfig hostConfig;

    private String hostnamePath;

    private String hostsPath;

    private String logPath;

    private String id;

    private String imageId;
    
    private String mountLabel;

    private String name;
    
    @JsonProperty("RestartCount")
    private Integer restartCount;

    @JsonProperty("NetworkSettings")
    private NetworkSettings networkSettings;

    @JsonProperty("Path")
    private String path;

    @JsonProperty("ProcessLabel")
    private String processLabel;

    @JsonProperty("ResolvConfPath")
    private String resolvConfPath;

    @JsonProperty("ExecIDs")
    private List<String> execIds;

    @JsonProperty("State")
    private ContainerState state;

    @JsonProperty("Volumes")
    private VolumeBinds volumes;

    @JsonProperty("VolumesRW")
    private VolumesRW volumesRW;

    @JsonProperty("Node")
    private Node node;

    @JsonProperty("Mounts")
    private List<Mount> mounts;

    @JsonProperty("GraphDriver")
    private GraphDriver graphDriver;

}
