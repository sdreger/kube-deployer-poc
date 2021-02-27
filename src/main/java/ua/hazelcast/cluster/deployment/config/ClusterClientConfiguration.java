package ua.hazelcast.cluster.deployment.config;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClusterClientConfiguration {

    @Bean
    public KubernetesClient clusterClient() {
        return new DefaultKubernetesClient();
    }
}
