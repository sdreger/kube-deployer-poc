package ua.hazelcast.cluster.deployment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class CreateDeploymentRequest {

    private String namespace;

    private String name;

    private Map<String, String> labels;

    private Integer containerPort;

    private Integer replicaCount;
}
