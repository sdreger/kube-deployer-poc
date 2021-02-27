package ua.hazelcast.cluster.deployment.service;

import ua.hazelcast.cluster.deployment.dto.CreateDeploymentRequest;
import ua.hazelcast.cluster.deployment.dto.DeploymentResponse;

public interface DeploymentService {

    /**
     * Creates a deployment in the given kubernetes cluster.
     *
     * @param createDeploymentRequest - deployment related metadata.
     * @return - a created deployment representation.
     */
    DeploymentResponse createDeployment(CreateDeploymentRequest createDeploymentRequest);
}
