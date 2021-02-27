package ua.hazelcast.cluster.deployment;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentCondition;
import io.fabric8.kubernetes.api.model.apps.DeploymentConditionBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.NamespacedKubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.HttpURLConnection;

import static ua.hazelcast.cluster.deployment.AbstractClusterTest.*;

@Profile("test")
@Configuration
public class TestConfig {

    private KubernetesMockServer kubernetesMockServer;

    private NamespacedKubernetesClient kubernetesClient;

    @PostConstruct
    public void init() {
        final Deployment testDeployment = createTestDeployment();
        kubernetesMockServer = new KubernetesMockServer( false);

        kubernetesMockServer.expect()
                .post()
                .withPath("/apis/apps/v1/namespaces/" + NS_DEFAULT + "/deployments")
                .andReturn(HttpURLConnection.HTTP_CREATED, testDeployment).always();

        kubernetesMockServer.expect()
                .get()
                .withPath("/apis/apps/v1/namespaces/" + NS_DEFAULT + "/deployments/" + DEPLOYMENT_NAME)
                .andReturn(HttpURLConnection.HTTP_OK, testDeployment).always();

        kubernetesMockServer.expect()
                .delete()
                .withPath("/apis/apps/v1/namespaces/" + NS_DEFAULT + "/deployments/" + DEPLOYMENT_NAME)
                .andReturn(HttpURLConnection.HTTP_OK, Boolean.TRUE).always();

        kubernetesClient = kubernetesMockServer.createClient();
    }

    @Bean
    public KubernetesClient getClient() {
        return kubernetesClient;
    }

    @PreDestroy
    public void shutDown() {
        kubernetesMockServer.shutdown();
    }

    protected Deployment createTestDeployment() throws KubernetesClientException {

        final DeploymentCondition deploymentCondition = new DeploymentConditionBuilder()
                .withStatus("True")
                .withMessage("Deployment has minimum availability.")
                .withReason("MinimumReplicasAvailable")
                .withType("Available")
                .build();

        final Deployment testDeployment = new DeploymentBuilder()
                .withNewStatus()
                    .withNewReadyReplicas(REPLICAS)
                    .withNewReplicas(REPLICAS)
                    .withConditions(deploymentCondition)
                .endStatus()
                .withNewMetadata()
                    .withName(DEPLOYMENT_NAME)
                    .addToLabels(DEPLOYMENT_LABELS)
                    .withCreationTimestamp(DEPLOYMENT_CREATION_TIMESTAMP)
                    .withUid(DEPLOYMENT_CREATION_UID)
                    .endMetadata()
                .withNewSpec()
                    .withReplicas(REPLICAS)
                    .withNewSelector()
                        .withMatchLabels(DEPLOYMENT_LABELS)
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .addToLabels(DEPLOYMENT_LABELS)
                        .endMetadata()
                        .withNewSpec()
                            .addNewContainer()
                                .withName(CONTAINER_NAME)
                                .withImage(CONTAINER_IMAGE)
                                .withPorts()
                                    .addNewPort()
                                        .withContainerPort(CONTAINER_PORT)
                                    .endPort()
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                .endSpec()
            .build();

        return testDeployment;
    }
}
