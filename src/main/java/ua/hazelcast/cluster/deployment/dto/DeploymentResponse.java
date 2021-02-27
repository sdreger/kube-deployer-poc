package ua.hazelcast.cluster.deployment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class DeploymentResponse {

    private String apiVersion;

    private String namespace;

    private String uid;

    private LocalDateTime creationTimestamp;

    private String name;

    private Map<String, String> labels;

    private Integer containerPort;

    private Integer replicaCount;
}
