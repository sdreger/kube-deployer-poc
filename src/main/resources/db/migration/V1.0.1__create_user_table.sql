CREATE TABLE users
(
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    PRIMARY KEY (email)
);

CREATE TABLE user_roles
(
    email VARCHAR(255) NOT NULL,
    role  VARCHAR(255) NOT NULL,
    PRIMARY KEY (email)
)
