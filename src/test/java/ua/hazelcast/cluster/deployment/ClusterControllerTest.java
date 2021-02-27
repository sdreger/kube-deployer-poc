package ua.hazelcast.cluster.deployment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    public void shouldDeleteDeployment() throws Exception {
        final DeploymentEntity testDeployment = createTestDeploymentEntity(DEPLOYMENT_NAME);
        this.mockMvc
                .perform(delete(URL_DEPLOYMENTS + "/" + testDeployment.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        final Optional<DeploymentEntity> removedEntity = deploymentRepository.findById(testDeployment.getId());
        assertThat(removedEntity).isEmpty();
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
