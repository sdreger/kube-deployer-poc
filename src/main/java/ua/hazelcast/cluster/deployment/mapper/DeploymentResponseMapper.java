package ua.hazelcast.cluster.deployment.mapper;

import org.mapstruct.Mapper;
import ua.hazelcast.cluster.deployment.dto.DeploymentResponse;
import ua.hazelcast.cluster.deployment.entity.DeploymentEntity;

@Mapper(componentModel = "spring")
public interface DeploymentResponseMapper {

    DeploymentResponse toDeploymentResponse(DeploymentEntity deploymentEntity);
}
