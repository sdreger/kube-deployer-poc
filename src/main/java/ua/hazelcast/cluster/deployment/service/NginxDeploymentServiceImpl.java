package ua.hazelcast.cluster.deployment.service;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.hazelcast.cluster.deployment.dto.CreateDeploymentRequest;
import ua.hazelcast.cluster.deployment.dto.DeploymentResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class NginxDeploymentServiceImpl implements DeploymentService {

    private static final String CONTAINER_IMAGE_VERSION = "1.14.2";

    private static final String CONTAINER_IMAGE = "nginx";

    private final KubernetesClient clusterClient;

    @Autowired
    public NginxDeploymentServiceImpl(KubernetesClient clusterClient) {
        this.clusterClient = clusterClient;
    }

    @Override
    public DeploymentResponse createDeployment(final CreateDeploymentRequest createDeploymentRequest) {
        log.debug("Creating a deployment: {}", createDeploymentRequest);

        final String namespace = createDeploymentRequest.getNamespace();
        final Map<String, String> deploymentLabels = createDeploymentRequest.getLabels();
        final String name = createDeploymentRequest.getName();
        final String container = String.format("%s:%s", CONTAINER_IMAGE, CONTAINER_IMAGE_VERSION);
        final Integer containerPort = createDeploymentRequest.getContainerPort();
        final Integer replicaCount = createDeploymentRequest.getReplicaCount();
        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(name)
                    .addToLabels(deploymentLabels)
                    .endMetadata()
                .withNewSpec()
                    .withReplicas(replicaCount)
                    .withNewSelector()
                        .withMatchLabels(deploymentLabels)
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .addToLabels(deploymentLabels)
                        .endMetadata()
                        .withNewSpec()
                            .addNewContainer()
                                .withName(name)
                                .withImage(container)
                                .withPorts()
                                    .addNewPort()
                                        .withContainerPort(containerPort)
                                    .endPort()
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                .endSpec()
            .build();

        final Deployment createdDeployment = clusterClient.apps()
                .deployments()
                .inNamespace(namespace)
                .createOrReplace(deployment);

        final LocalDateTime creationTime = LocalDateTime
                .parse(createdDeployment.getMetadata().getCreationTimestamp(), DateTimeFormatter.ISO_DATE_TIME);
        return DeploymentResponse.builder()
                .apiVersion(createdDeployment.getApiVersion())
                .namespace(namespace)
                .name(name)
                .uid(createdDeployment.getMetadata().getUid())
                .creationTimestamp(creationTime)
                .labels(deploymentLabels)
                .replicaCount(replicaCount)
                .containerPort(containerPort)
                .build();
    }
}
