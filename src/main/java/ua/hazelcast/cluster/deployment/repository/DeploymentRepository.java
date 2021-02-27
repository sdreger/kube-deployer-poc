package ua.hazelcast.cluster.deployment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.hazelcast.cluster.deployment.entity.DeploymentEntity;

@Repository
public interface DeploymentRepository extends JpaRepository<DeploymentEntity, Long> {
}
