package ua.hazelcast.cluster.deployment;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.hazelcast.cluster.deployment.repository.DeploymentRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@ActiveProfiles("test")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractClusterTest.PostgresContainerInitializer.class)
public abstract class AbstractClusterTest {

    protected static final String API_VERSION = "apps/v1";

    protected static final String NS_DEFAULT = "default";

    protected static final String DEPLOYMENT_NAME = "test-nginx-deployment";

    protected static final Integer REPLICAS = 2;

    protected static final Map<String, String> DEPLOYMENT_LABELS = Map.of("app", "nginx");

    protected static final String CONTAINER_NAME = "nginx";

    protected static final String CONTAINER_IMAGE = "nginx:1.14.2";

    protected static final int CONTAINER_PORT = 80;

    protected static final String DEPLOYMENT_CREATION_TIMESTAMP = "2021-02-27T17:45:00";

    protected static final LocalDateTime DEPLOYMENT_CREATION_DATE_TIME =
            LocalDateTime.parse(DEPLOYMENT_CREATION_TIMESTAMP, DateTimeFormatter.ISO_DATE_TIME);

    protected static final String DEPLOYMENT_CREATION_UID = "7d201fa4-d2fc-4a25-89c0-26a671bc9007";

    protected static final UUID DEPLOYMENT_CREATION_UUID = UUID.fromString(DEPLOYMENT_CREATION_UID);

    @Autowired
    protected KubernetesClient client;

    @Autowired
    protected DeploymentRepository deploymentRepository;

    public static PostgreSQLContainer<?> postgreDBContainer = new PostgreSQLContainer<>("postgres:12.6");

    static {
        postgreDBContainer.start();
    }

    public static class PostgresContainerInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(final ConfigurableApplicationContext applicationContext) {

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + postgreDBContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreDBContainer.getUsername(),
                    "spring.datasource.password=" + postgreDBContainer.getPassword()
            );
        }
    }
}
