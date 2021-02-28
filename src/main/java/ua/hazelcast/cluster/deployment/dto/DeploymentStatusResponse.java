package ua.hazelcast.cluster.deployment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentStatusResponse {

    private String reason;

    private String message;

    private boolean rolloutComplete;
}
