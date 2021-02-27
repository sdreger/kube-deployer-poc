package ua.hazelcast.cluster.deployment.config;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class ClusterClientConfiguration {

    @Bean
    public KubernetesClient clusterClient() {
        return new DefaultKubernetesClient();
    }
}
