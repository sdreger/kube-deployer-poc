package ua.hazelcast.cluster.deployment.service;

import ua.hazelcast.cluster.deployment.dto.CreateDeploymentRequest;
import ua.hazelcast.cluster.deployment.dto.DeploymentResponse;

public interface DeploymentService {

    DeploymentResponse createDeployment(CreateDeploymentRequest createDeploymentRequest);
}
