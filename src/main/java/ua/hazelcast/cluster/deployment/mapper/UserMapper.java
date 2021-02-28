package ua.hazelcast.cluster.deployment.mapper;

import org.mapstruct.Mapper;
import ua.hazelcast.cluster.deployment.dto.SignupRequest;
import ua.hazelcast.cluster.deployment.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEntity toUserEntity(SignupRequest request);
}
