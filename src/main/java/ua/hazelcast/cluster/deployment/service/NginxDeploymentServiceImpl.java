package ua.hazelcast.cluster.deployment.service;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hazelcast.cluster.deployment.dto.CreateDeploymentRequest;
import ua.hazelcast.cluster.deployment.dto.DeploymentResponse;
import ua.hazelcast.cluster.deployment.dto.DeploymentStatusResponse;
import ua.hazelcast.cluster.deployment.entity.DeploymentEntity;
import ua.hazelcast.cluster.deployment.exception.ApplicationException;
import ua.hazelcast.cluster.deployment.mapper.DeploymentResponseMapper;
import ua.hazelcast.cluster.deployment.repository.DeploymentRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class NginxDeploymentServiceImpl implements DeploymentService {

    private static final String CONTAINER_IMAGE_VERSION = "1.14.2";

    private static final String CONTAINER_IMAGE = "nginx";

    private static final String UNKNOWN = "Unknown";

    private static final String CONDITION_TYPE_AVAILABLE = "Available";

    private static final String CONDITION_REASON_AVAILABLE = "MinimumReplicasAvailable";

    private static final long REPEAT_STATUS_CHECK_INTERVAL_SECONDS = 1L;

    private static final int REPEAT_STATUS_CHECK_ROUNDS = 30;

    private final KubernetesClient clusterClient;

    private final DeploymentRepository deploymentRepository;

    private final DeploymentResponseMapper deploymentResponseMapper;

    @Autowired
    public NginxDeploymentServiceImpl(final KubernetesClient clusterClient,
                                      final DeploymentRepository deploymentRepository,
                                      final DeploymentResponseMapper deploymentResponseMapper) {
        this.clusterClient = clusterClient;
        this.deploymentRepository = deploymentRepository;
        this.deploymentResponseMapper = deploymentResponseMapper;
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

        try {
            final Deployment createdDeployment = clusterClient.apps()
                    .deployments()
                    .inNamespace(namespace)
                    .createOrReplace(deployment);

            final LocalDateTime creationTime = LocalDateTime
                    .parse(createdDeployment.getMetadata().getCreationTimestamp(), DateTimeFormatter.ISO_DATE_TIME);
            final UUID uid = UUID.fromString(createdDeployment.getMetadata().getUid());

            final DeploymentEntity deploymentEntity = new DeploymentEntity();
            deploymentEntity.setApiVersion(createdDeployment.getApiVersion());
            deploymentEntity.setNamespace(namespace);
            deploymentEntity.setName(name);
            deploymentEntity.setUid(uid);
            deploymentEntity.setCreationTimestamp(creationTime);
            deploymentEntity.setLabels(deploymentLabels);
            deploymentEntity.setReplicaCount(replicaCount);
            deploymentEntity.setContainerPort(containerPort);
            deploymentRepository.save(deploymentEntity);

            return deploymentResponseMapper.toDeploymentResponse(deploymentEntity);
        } catch (final KubernetesClientException e) {
            log.error("Api call error: {}", e.getMessage());
            throw new ApplicationException(e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentResponse getDeployment(final Long deploymentId) {
        return deploymentResponseMapper.toDeploymentResponse(deploymentRepository.getOne(deploymentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeploymentResponse> getDeployments(final Pageable pageable) {
        final Page<DeploymentEntity> page = deploymentRepository.findAll(pageable);
        final List<DeploymentResponse> pageList = page.getContent()
                .stream()
                .map(deploymentResponseMapper::toDeploymentResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(pageList, pageable, page.getTotalElements());
    }

    @Override
    public void deleteDeployment(final Long deploymentId) {
        final DeploymentEntity existingDeployment = deploymentRepository.getOne(deploymentId);
        try {
            clusterClient.apps()
                    .deployments()
                    .inNamespace(existingDeployment.getNamespace())
                    .withName(existingDeployment.getName())
                    .delete();
            deploymentRepository.delete(existingDeployment);
        } catch (final KubernetesClientException e) {
            log.error("Api call error: {}", e.getMessage());
            throw new ApplicationException(e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentStatusResponse getDeploymentRollingStatus(final Long deploymentId, final boolean watch) {
        log.debug("Getting a deployment rolling status. Id {}. Watch: {}", deploymentId, watch);

        final DeploymentEntity existingDeployment = deploymentRepository.getOne(deploymentId);
        DeploymentStatusResponse deploymentStatus = getDeploymentStatus(existingDeployment);
        if (deploymentStatus.isRolloutComplete() || !watch) {
            return deploymentStatus;
        }

        int checkRound = 0;
        while (!deploymentStatus.isRolloutComplete() || checkRound == REPEAT_STATUS_CHECK_ROUNDS) {
            try {
                TimeUnit.SECONDS.sleep(REPEAT_STATUS_CHECK_INTERVAL_SECONDS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            deploymentStatus = getDeploymentStatus(existingDeployment);
            checkRound++;
        }

        return deploymentStatus;
    }

    private DeploymentStatusResponse getDeploymentStatus(final DeploymentEntity existingDeployment) {
        try {
            final Deployment deployment = clusterClient.apps()
                    .deployments()
                    .inNamespace(existingDeployment.getNamespace())
                    .withName(existingDeployment.getName())
                    .get();
            final Optional<DeploymentCondition> lastCondition = getLastCondition(deployment);
            if (lastCondition.isEmpty()) {
                return new DeploymentStatusResponse(UNKNOWN, UNKNOWN, false);
            }

            final DeploymentCondition condition = lastCondition.get();
            return new DeploymentStatusResponse(
                    condition.getReason(),
                    condition.getMessage(),
                    isRolloutComplete(condition)
            );
        } catch (final KubernetesClientException e) {
            log.error("Api call error: {}", e.getMessage());
            throw new ApplicationException(e.getMessage());
        }
    }

    private Optional<DeploymentCondition> getLastCondition(final Deployment deployment) {
        return deployment.getStatus()
                .getConditions()
                .stream()
                .max(Comparator.comparing(DeploymentCondition::getLastTransitionTime));
    }

    private boolean isRolloutComplete(final DeploymentCondition deploymentCondition) {
        return deploymentCondition.getType().equals(CONDITION_TYPE_AVAILABLE)
                && deploymentCondition.getReason().equals(CONDITION_REASON_AVAILABLE);
    }
}
