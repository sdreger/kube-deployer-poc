package ua.hazelcast.cluster.deployment.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table(name = "deployment")
public class DeploymentEntity {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "deployment_id_seq", sequenceName = "deployment_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deployment_id_seq")
    private Long id;

    @Column(name = "uid")
    private UUID uid;

    @Column(name = "api_version")
    private String apiVersion;

    @Column(name = "namespace")
    private String namespace;

    @Column(name = "creation_timestamp")
    private LocalDateTime creationTimestamp;

    @Column(name = "name")
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "labels", joinColumns = @JoinColumn(name = "label_id"))
    @MapKeyColumn(name = "label_key")
    @Column(name = "label_value")
    private Map<String, String> labels;

    @Column(name = "container_port")
    private Integer containerPort;

    @Column(name = "replica_count")
    private Integer replicaCount;
}
