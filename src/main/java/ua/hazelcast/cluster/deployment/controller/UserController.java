package ua.hazelcast.cluster.deployment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ua.hazelcast.cluster.deployment.dto.SignupRequest;
import ua.hazelcast.cluster.deployment.service.UserService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(final @RequestBody @Valid SignupRequest request) {
        userService.createUser(request);
    }

    // TODO: add the '/me' endpoint
    // TODO: add an endpoint to remove user
}
