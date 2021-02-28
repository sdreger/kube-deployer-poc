package ua.hazelcast.cluster.deployment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import ua.hazelcast.cluster.deployment.dto.CreateDeploymentRequest;
import ua.hazelcast.cluster.deployment.dto.DeploymentResponse;
import ua.hazelcast.cluster.deployment.dto.DeploymentStatusResponse;
import ua.hazelcast.cluster.deployment.entity.DeploymentEntity;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ClusterControllerTest extends AbstractClusterTest {

    private static final String URL_DEPLOYMENTS = "/deployment";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldCreateDeployment() throws Exception {
        final CreateDeploymentRequest request = new CreateDeploymentRequest();
        request.setName(DEPLOYMENT_NAME);
        request.setNamespace(NS_DEFAULT);
        request.setLabels(DEPLOYMENT_LABELS);
        request.setReplicaCount(REPLICAS);
        request.setContainerPort(CONTAINER_PORT);

        final MockHttpServletResponse response = this.mockMvc.perform(post(URL_DEPLOYMENTS)
                .contentType(MediaType.APPLICATION_JSON)
    //                        .header(HttpHeaders.AUTHORIZATION, getAccessTokenForSystemUser())
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        final DeploymentResponse deploymentResponse =
                objectMapper.readValue(response.getContentAsString(), DeploymentResponse.class);
        assertDeploymentResponse(deploymentResponse, DEPLOYMENT_NAME);

        final Optional<DeploymentEntity> deploymentEntity = deploymentRepository.findById(deploymentResponse.getId());
        assertThat(deploymentEntity).isPresent();
        final DeploymentEntity deployment = deploymentEntity.get();
        assertThat(deployment.getId()).isNotNull();
        assertThat(deployment.getNamespace()).isEqualTo(NS_DEFAULT);
        assertThat(deployment.getName()).isEqualTo(DEPLOYMENT_NAME);
        assertThat(deployment.getLabels()).isEqualTo(DEPLOYMENT_LABELS);
        assertThat(deployment.getContainerPort()).isEqualTo(CONTAINER_PORT);
        assertThat(deployment.getReplicaCount()).isEqualTo(REPLICAS);
        assertThat(deployment.getUid()).isEqualTo(DEPLOYMENT_CREATION_UUID);
        assertThat(deployment.getCreationTimestamp()).isEqualTo(DEPLOYMENT_CREATION_TIMESTAMP);

        deploymentRepository.delete(deployment);
    }

    @Test
    public void shouldNotCreateDeploymentWithoutNamespace() throws Exception {
        final CreateDeploymentRequest request = new CreateDeploymentRequest();
        request.setNamespace(null);
        request.setName(DEPLOYMENT_NAME);
        request.setLabels(DEPLOYMENT_LABELS);
        request.setReplicaCount(REPLICAS);
        request.setContainerPort(CONTAINER_PORT);

        this.mockMvc.perform(post(URL_DEPLOYMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").exists())
                .andExpect(jsonPath("$.violations[0].field").value("namespace"))
                .andExpect(jsonPath("$.violations[0].message")
                        .value("Namespace must not be blank"));
    }

    @Test
    public void shouldNotCreateDeploymentWithoutName() throws Exception {
        final CreateDeploymentRequest request = new CreateDeploymentRequest();
        request.setNamespace(NS_DEFAULT);
        request.setName(null);
        request.setLabels(DEPLOYMENT_LABELS);
        request.setReplicaCount(REPLICAS);
        request.setContainerPort(CONTAINER_PORT);

        this.mockMvc.perform(post(URL_DEPLOYMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").exists())
                .andExpect(jsonPath("$.violations[0].field").value("name"))
                .andExpect(jsonPath("$.violations[0].message").value("Name must not be blank"));
    }

    @Test
    public void shouldNotCreateDeploymentWithoutLabels() throws Exception {
        final CreateDeploymentRequest request = new CreateDeploymentRequest();
        request.setNamespace(NS_DEFAULT);
        request.setName(DEPLOYMENT_NAME);
        request.setLabels(Collections.emptyMap());
        request.setReplicaCount(REPLICAS);
        request.setContainerPort(CONTAINER_PORT);

        this.mockMvc.perform(post(URL_DEPLOYMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").exists())
                .andExpect(jsonPath("$.violations[0].field").value("labels"))
                .andExpect(jsonPath("$.violations[0].message")
                        .value("Labels must contain at least one entry"));
    }

    @Test
    public void shouldNotCreateDeploymentWithoutReplicas() throws Exception {
        final CreateDeploymentRequest request = new CreateDeploymentRequest();
        request.setNamespace(NS_DEFAULT);
        request.setName(DEPLOYMENT_NAME);
        request.setLabels(DEPLOYMENT_LABELS);
        request.setReplicaCount(null);
        request.setContainerPort(CONTAINER_PORT);

        this.mockMvc.perform(post(URL_DEPLOYMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").exists())
                .andExpect(jsonPath("$.violations[0].field").value("replicaCount"))
                .andExpect(jsonPath("$.violations[0].message")
                        .value("ReplicaCount must not be null"));
    }

    @Test
    public void shouldNotCreateDeploymentWithNegativeReplicas() throws Exception {
        final CreateDeploymentRequest request = new CreateDeploymentRequest();
        request.setNamespace(NS_DEFAULT);
        request.setName(DEPLOYMENT_NAME);
        request.setLabels(DEPLOYMENT_LABELS);
        request.setReplicaCount(-1);
        request.setContainerPort(CONTAINER_PORT);

        this.mockMvc.perform(post(URL_DEPLOYMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").exists())
                .andExpect(jsonPath("$.violations[0].field").value("replicaCount"))
                .andExpect(jsonPath("$.violations[0].message")
                        .value("ReplicaCount must be a positive value"));
    }

    @Test
    public void shouldNotCreateDeploymentWithDuplicatedName() throws Exception {

        final DeploymentEntity testDeployment = createTestDeploymentEntity(DEPLOYMENT_NAME);

        final CreateDeploymentRequest request = new CreateDeploymentRequest();
        request.setNamespace(NS_DEFAULT);
        request.setName(DEPLOYMENT_NAME);
        request.setLabels(DEPLOYMENT_LABELS);
        request.setReplicaCount(REPLICAS);
        request.setContainerPort(CONTAINER_PORT);

        this.mockMvc.perform(post(URL_DEPLOYMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").exists())
                .andExpect(jsonPath("$.violations[0].field").value("name"))
                .andExpect(jsonPath("$.violations[0].message")
                        .value("Deployment with the name " + DEPLOYMENT_NAME + " already exists"));

        deploymentRepository.delete(testDeployment);
    }

    @Test
    public void shouldCreateDeploymentClusterException() throws Exception {
        final CreateDeploymentRequest request = new CreateDeploymentRequest();
        request.setName(DEPLOYMENT_NAME);
        request.setNamespace(NS_DEFAULT);
        request.setLabels(DEPLOYMENT_LABELS);
        request.setReplicaCount(REPLICAS);
        request.setContainerPort(CONTAINER_PORT);

        final String errorMessage = "API call error";
        doThrow(new KubernetesClientException(errorMessage)).when(client).apps();

        this.mockMvc.perform(post(URL_DEPLOYMENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.title").value("Application Exception"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.detail").value(errorMessage));
    }

    @Test
    public void shouldReturnDeployment() throws Exception {
        final DeploymentEntity testDeployment = createTestDeploymentEntity(DEPLOYMENT_NAME);
        final MockHttpServletResponse response = this.mockMvc
                .perform(get(URL_DEPLOYMENTS + "/" + testDeployment.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        final DeploymentResponse deploymentResponse =
                objectMapper.readValue(response.getContentAsString(), DeploymentResponse.class);
        assertDeploymentResponse(deploymentResponse, DEPLOYMENT_NAME);

        deploymentRepository.delete(testDeployment);
    }

    @Test
    public void shouldNotReturnNonExistingDeployment() throws Exception {

        this.mockMvc
                .perform(get(URL_DEPLOYMENTS + "/" + Long.MAX_VALUE))
                .andDo(print())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").exists())
                .andExpect(jsonPath("$.violations[0].field")
                        .value("getDeployment.deploymentId"))
                .andExpect(jsonPath("$.violations[0].message")
                        .value("Deployment with ID 9223372036854775807 doesn't exist"));
    }

    @Test
    public void shouldReturnDeploymentList() throws Exception {
        final DeploymentEntity testDeployment1 = createTestDeploymentEntity(DEPLOYMENT_NAME + 1);
        final DeploymentEntity testDeployment2 = createTestDeploymentEntity(DEPLOYMENT_NAME + 2);
        final MockHttpServletResponse response = this.mockMvc
                .perform(get(URL_DEPLOYMENTS))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        final ResponsePage<DeploymentResponse> deploymentResponse = objectMapper
                .readValue(response.getContentAsString(), new TypeReference<ResponsePage<DeploymentResponse>>() {
                });
        assertThat(deploymentResponse).isNotNull();
        assertThat(deploymentResponse.getTotalElements()).isEqualTo(2);
        assertThat(deploymentResponse.getContent()).isNotNull().hasSize(2);
        assertDeploymentResponse(deploymentResponse.getContent().get(0), testDeployment1.getName());
        assertDeploymentResponse(deploymentResponse.getContent().get(1), testDeployment2.getName());

        deploymentRepository.delete(testDeployment1);
        deploymentRepository.delete(testDeployment2);
    }

    @Test
    public void shouldReturnDeploymentStatus() throws Exception {
        final DeploymentEntity testDeployment = createTestDeploymentEntity(DEPLOYMENT_NAME);
        final MockHttpServletResponse response = this.mockMvc
                .perform(get(URL_DEPLOYMENTS + "/rolling/status/" + testDeployment.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        final DeploymentStatusResponse deploymentStatusResponse =
                objectMapper.readValue(response.getContentAsString(), DeploymentStatusResponse.class);
        assertThat(deploymentStatusResponse).isNotNull();
        assertThat(deploymentStatusResponse.getMessage()).isEqualTo("Deployment has minimum availability.");
        assertThat(deploymentStatusResponse.getReason()).isEqualTo("MinimumReplicasAvailable");
        assertThat(deploymentStatusResponse.isRolloutComplete()).isTrue();

        deploymentRepository.delete(testDeployment);
    }

    @Test
    public void shouldNotReturnNonExistingDeploymentStatus() throws Exception {

        this.mockMvc
                .perform(get(URL_DEPLOYMENTS + "/rolling/status/" + Long.MAX_VALUE))
                .andDo(print())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").exists())
                .andExpect(jsonPath("$.violations[0].field")
                        .value("getDeploymentRollingStatus.deploymentId"))
                .andExpect(jsonPath("$.violations[0].message")
                        .value("Deployment with ID 9223372036854775807 doesn't exist"));
    }

    @Test
    public void shouldNotReturnDeploymentStatusClusterException() throws Exception {

        final DeploymentEntity testDeployment = createTestDeploymentEntity(DEPLOYMENT_NAME);

        final String errorMessage = "API call error";
        doThrow(new KubernetesClientException(errorMessage)).when(client).apps();

        this.mockMvc
                .perform(get(URL_DEPLOYMENTS + "/rolling/status/" + testDeployment.getId()))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.title").value("Application Exception"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.detail").value(errorMessage));

        deploymentRepository.delete(testDeployment);
    }

    @Test
    public void shouldDeleteDeployment() throws Exception {
        final DeploymentEntity testDeployment = createTestDeploymentEntity(DEPLOYMENT_NAME);
        this.mockMvc
                .perform(delete(URL_DEPLOYMENTS + "/" + testDeployment.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        final Optional<DeploymentEntity> removedEntity = deploymentRepository.findById(testDeployment.getId());
        assertThat(removedEntity).isEmpty();
    }

    @Test
    public void shouldNotDeleteNonExistingDeployment() throws Exception {

        this.mockMvc
                .perform(delete(URL_DEPLOYMENTS + "/" + Long.MAX_VALUE))
                .andDo(print())
                .andExpect(content().contentType(APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").exists())
                .andExpect(jsonPath("$.violations[0].field")
                        .value("deleteDeployment.deploymentId"))
                .andExpect(jsonPath("$.violations[0].message")
                        .value("Deployment with ID 9223372036854775807 doesn't exist"));
    }

    @Test
    public void shouldNotDeleteDeploymentClusterException() throws Exception {

        final DeploymentEntity testDeployment = createTestDeploymentEntity(DEPLOYMENT_NAME);

        final String errorMessage = "API call error";
        doThrow(new KubernetesClientException(errorMessage)).when(client).apps();

        this.mockMvc
                .perform(delete(URL_DEPLOYMENTS + "/" + testDeployment.getId()))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.title").value("Application Exception"))
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.detail").value(errorMessage));

        deploymentRepository.delete(testDeployment);
    }


    private void assertDeploymentResponse(final DeploymentResponse deploymentResponse, final String name) {
        assertThat(deploymentResponse).isNotNull();
        assertThat(deploymentResponse.getApiVersion()).isEqualTo(API_VERSION);
        assertThat(deploymentResponse.getId()).isNotNull();
        assertThat(deploymentResponse.getNamespace()).isEqualTo(NS_DEFAULT);
        assertThat(deploymentResponse.getName()).isEqualTo(name);
        assertThat(deploymentResponse.getLabels()).isEqualTo(DEPLOYMENT_LABELS);
        assertThat(deploymentResponse.getContainerPort()).isEqualTo(CONTAINER_PORT);
        assertThat(deploymentResponse.getReplicaCount()).isEqualTo(REPLICAS);
        assertThat(deploymentResponse.getUid()).isEqualTo(DEPLOYMENT_CREATION_UUID);
        assertThat(deploymentResponse.getCreationTimestamp()).isEqualTo(DEPLOYMENT_CREATION_TIMESTAMP);
    }

    private DeploymentEntity createTestDeploymentEntity(final String name) {
        final DeploymentEntity deploymentEntity = new DeploymentEntity();
        deploymentEntity.setApiVersion(API_VERSION);
        deploymentEntity.setNamespace(NS_DEFAULT);
        deploymentEntity.setName(name);
        deploymentEntity.setLabels(DEPLOYMENT_LABELS);
        deploymentEntity.setContainerPort(CONTAINER_PORT);
        deploymentEntity.setReplicaCount(REPLICAS);
        deploymentEntity.setUid(DEPLOYMENT_CREATION_UUID);
        deploymentEntity.setCreationTimestamp(DEPLOYMENT_CREATION_DATE_TIME);
        return deploymentRepository.save(deploymentEntity);
    }
}
