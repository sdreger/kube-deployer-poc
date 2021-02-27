package ua.hazelcast.cluster.deployment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping(value = "/deployment", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeploymentController {

    private final DeploymentService deploymentService;

    @Autowired
    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DeploymentResponse createDeployment(final @RequestBody CreateDeploymentRequest createDeploymentRequest) {
        return deploymentService.createDeployment(createDeploymentRequest);
    }

    @GetMapping
    public Page<DeploymentResponse> getDeployments(final @PageableDefault(size = 50) Pageable pageable) {
        return deploymentService.getDeployments(pageable);
    }
}
