package ua.hazelcast.cluster.deployment.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DeploymentStatusResponse {

    private final String reason;

    private final String message;

    private final boolean rolloutComplete;
}
