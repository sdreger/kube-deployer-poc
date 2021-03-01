# The app for managing Kubernetes cluster deployments.

## Current supported operations (Nginx deployment only):
- Create a deployment
- Get a deployment rollout status (blocking and non-blocking modes)
- Get a deployment from the DB
- Get a deployment list from the DB
- Delete a deployment

## Workstation preparation
1. Install a virtual machine manager VirtualBox, or VMWare
2. Install Minikube: https://minikube.sigs.k8s.io/docs/start/
3. Install kubectl: https://kubernetes.io/docs/tasks/tools/install-kubectl/
4. Start the Minikube VM: `minikube start`
5. Install Docker: https://docs.docker.com/get-docker/
6. Install Docker-compose: https://docs.docker.com/compose/install/

## Run application tests
- `./gradlew clean build`

## Run the app in the embedded mode
- `./gradlew clean build -x test && docker-compose -f docker-compose.yml up --build`
- Open the `src/test/rest/client.rest` file
- If you are using th IDEA, you could make requests directly from your IDE
- Check the application health: 
```shell
GET http://localhost:8080/actuator/health
```
- Create a user:
```shell
POST http://localhost:8080/api/user/signup
Content-Type: application/json

{
  "email": "test@hazelcast.com",
  "password": "testSecurePassword",
  "firstName": "Test First Name",
  "lastName": "Test Last Name",
  "roles": ["ROLE_USER"]
}
```
- Get an access token:
```shell
POST http://localhost:8080/api/user/login
Content-Type: application/json

{
  "email": "test@hazelcast.com",
  "password": "testSecurePassword"
}
```
- Create a deployment:
```shell
POST http://localhost:8080/api/deployment
Content-Type: application/json
Authorization: Bearer {{access_token}}

{
  "namespace": "default",
  "name": "test-nginx-1",
  "labels": {
    "app": "nignx",
    "env": "uat"
  },
  "containerPort": 80,
  "replicaCount": 1
}
```
- Get a deployment rollout status:
```shell
GET http://localhost:8080/api/deployment/rolling/status/1?watch=true
Authorization: Bearer {{access_token}}
```
- Get a deployment list:
```shell
GET http://localhost:8080/api/deployment
Authorization: Bearer {{access_token}}
```
- Get a deployment by ID:
```shell
GET http://localhost:8080/api/deployment/{{deploymentId}}
Authorization: Bearer {{access_token}}
```
- Delete a deployment by ID:
```shell
DELETE http://localhost:8080/api/deployment/1
Authorization: Bearer {{access_token}}
```
- Open the: http://localhost:9090 and login with the following credentials
    - Server: postgres
    - Username: test
    - Password: test
    - Database: kube_deployer
- Investigate the DB with the Adminer tool

## Build the Docker container:
- `./gradlew jibDockerBuild`
- or use the `Dockerfile` to build the container

## Run the Postgres Docker container:
```shell
docker run --network host \
  --name postgres \
  --rm \
  -e POSTGRES_USER=test \
  -e POSTGRES_PASSWORD=test \
  -e POSTGRES_DB=kube_deployer \
  postgres:12.6
```
## Run the Docker container (Minikube and Postgres should be running):
```shell
docker run --rm \
  -p 8080:8080 \
  -v ~/.minikube:/home/root/.minikube \
  -e SPRING_DATASOURCE_HOST=127.0.0.1 \
  -e SPRING_DATASOURCE_PORT=5432 \
  -e SPRING_DATASOURCE_USERNAME=test \
  -e SPRING_DATASOURCE_PASSWORD=test \
  -e SPRING_FLYWAY_ENABLED=true \
  -e KUBERNETES_MASTER=https://192.168.99.100:8443 \
  -e KUBERNETES_CERTS_CA_FILE=/home/root/.minikube/ca.crt \
  -e KUBERNETES_CERTS_CLIENT_FILE=/home/root/.minikube/profiles/minikube/client.crt \
  -e KUBERNETES_CERTS_CLIENT_KEY_FILE=/home/root/.minikube/profiles/minikube/client.key \
  registry.hazelcast.com/deployment:latest
```

#### Use your favorite REST client to interact with the application.
