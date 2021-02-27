package ua.hazelcast.cluster.deployment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ua.hazelcast.cluster.deployment.dto.CreateDeploymentRequest;
import ua.hazelcast.cluster.deployment.dto.DeploymentResponse;
import ua.hazelcast.cluster.deployment.service.DeploymentService;

/**
 * REST controller which allows to manage deployments in the given kubernetes cluster.
 */
@RestController
@RequestMapping(value = "/deployment",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class DeploymentController {

    private final DeploymentService deploymentService;

    @Autowired
    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeploymentResponse createDeployment(@RequestBody CreateDeploymentRequest createDeploymentRequest) {
        return deploymentService.createDeployment(createDeploymentRequest);
    }
}
