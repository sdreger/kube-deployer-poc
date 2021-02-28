package ua.hazelcast.cluster.deployment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.hazelcast.cluster.deployment.dto.CreateDeploymentRequest;
import ua.hazelcast.cluster.deployment.dto.DeploymentResponse;
import ua.hazelcast.cluster.deployment.dto.DeploymentStatusResponse;
import ua.hazelcast.cluster.deployment.service.DeploymentService;
import ua.hazelcast.cluster.deployment.validation.DeploymentExists;

import javax.validation.Valid;

/**
 * REST controller which allows to manage deployments in the given kubernetes cluster.
 */
@Validated
@RestController
@RequestMapping(value = "/api/deployment", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeploymentController {

    private final DeploymentService deploymentService;

    @Autowired
    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DeploymentResponse createDeployment(final @RequestBody @Valid CreateDeploymentRequest createDeploymentRequest) {
        return deploymentService.createDeployment(createDeploymentRequest);
    }

    @GetMapping("/rolling/status/{deploymentId}")
    public DeploymentStatusResponse getDeploymentRollingStatus(
            final @PathVariable @DeploymentExists Long deploymentId,
            final @RequestParam(name = "watch", defaultValue = "true", required = false) boolean watch) {
        return deploymentService.getDeploymentRollingStatus(deploymentId, watch);
    }

    @GetMapping
    public Page<DeploymentResponse> getDeployments(final @PageableDefault(size = 50) Pageable pageable) {
        return deploymentService.getDeployments(pageable);
    }

    @GetMapping("/{deploymentId}")
    public DeploymentResponse getDeployment(final @PathVariable @DeploymentExists Long deploymentId) {
        return deploymentService.getDeployment(deploymentId);
    }

    @DeleteMapping("/{deploymentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeployment(final @PathVariable @DeploymentExists Long deploymentId) {
        deploymentService.deleteDeployment(deploymentId);
    }
}
