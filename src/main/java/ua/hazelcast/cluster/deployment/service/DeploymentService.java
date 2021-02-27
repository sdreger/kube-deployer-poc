package ua.hazelcast.cluster.deployment.service;

import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ua.hazelcast.cluster.deployment.dto.CreateDeploymentRequest;
import ua.hazelcast.cluster.deployment.dto.DeploymentResponse;
import ua.hazelcast.cluster.deployment.dto.DeploymentStatusResponse;

public interface DeploymentService {

    /**
     * Creates a deployment in the kubernetes cluster.
     *
     * @param createDeploymentRequest - deployment related metadata.
     * @return - a created deployment representation.
     */
    DeploymentResponse createDeployment(CreateDeploymentRequest createDeploymentRequest);

    /**
     * Get an existing deployment from the kubernetes cluster.
     *
     * @param pageable - pagination object.
     * @return - existing deployments list representation.
     */
    Page<DeploymentResponse> getDeployments(Pageable pageable);

    /**
     * Get an existing deployment from the kubernetes cluster.
     *
     * @param deploymentId - deployment id for retrieval.
     * @return - an existing deployment representation.
     */
    DeploymentResponse getDeployment(Long deploymentId);

    /**
     * Delete an existing deployment from the kubernetes cluster.
     *
     * @param deploymentId - deployment id for deletion.
     */
    void deleteDeployment(Long deploymentId);


    /**
     * Get a rolling status of an existing deployment from the kubernetes cluster.
     *
     * @param deploymentId - deployment id for retrieval.
     * @return - a deployment rolling status.
     */
    DeploymentStatusResponse getDeploymentRollingStatus(Long deploymentId, boolean watch);
}
