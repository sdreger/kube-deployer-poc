package ua.hazelcast.cluster.deployment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

// TODO: move all the messages into a property file for i18n
@Data
@NoArgsConstructor
public class CreateDeploymentRequest {

    @NotBlank(message = "Namespace must not be blank")
    private String namespace;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotEmpty(message = "Labels must contain at least one entry")
    private Map<String, String> labels;

    // Optional, will be set to the default value if not present
    private Integer containerPort;

    @NotNull(message = "ReplicaCount must not be null")
    @Min(value = 0, message = "ReplicaCount must be a positive value")
    private Integer replicaCount;
}
