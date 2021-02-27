package ua.hazelcast.cluster.deployment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class DeploymentResponse {

    private String apiVersion;

    private String namespace;

    private UUID uid;

    private LocalDateTime creationTimestamp;

    private String name;

    private Map<String, String> labels;

    private Integer containerPort;

    private Integer replicaCount;
}
