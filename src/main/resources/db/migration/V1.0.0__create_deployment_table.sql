CREATE SEQUENCE deployment_id_seq;

CREATE TABLE deployment
(
    id                 BIGINT       NOT NULL DEFAULT nextval('deployment_id_seq'),
    uid                VARCHAR(36)  NOT NULL,
    name               VARCHAR(255) NOT NULL,
    api_version        VARCHAR(16)  NOT NULL,
    namespace          VARCHAR(255) NOT NULL,
    creation_timestamp TIMESTAMP    NOT NULL,
    container_port     INTEGER,
    replica_count      INTEGER      NOT NULL,
    PRIMARY KEY (id)
);

create table labels
(
    label_id    BIGINT,
    label_key   VARCHAR(255),
    label_value VARCHAR(255)
);
