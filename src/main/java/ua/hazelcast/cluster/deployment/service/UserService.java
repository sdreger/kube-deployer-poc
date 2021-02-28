package ua.hazelcast.cluster.deployment.service;

import ua.hazelcast.cluster.deployment.dto.SignupRequest;

public interface UserService {

    /**
     * Create a user and store it in the DB.
     *
     * @param request - user creation request.
     */
    void createUser(SignupRequest request);
}
