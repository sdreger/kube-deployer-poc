package ua.hazelcast.cluster.deployment.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Get an existing deployment from the given kubernetes cluster.
     *
     * @param deploymentId - deployment id for retrieval.
     * @return - an existing deployment representation.
     */
    DeploymentResponse getDeployment(Long deploymentId);

    /**
     * Get an existing deployment from the given kubernetes cluster.
     *
     * @param pageable - pagination object.
     * @return - existing deployments list representation.
     */
    Page<DeploymentResponse> getDeployments(Pageable pageable);
}
