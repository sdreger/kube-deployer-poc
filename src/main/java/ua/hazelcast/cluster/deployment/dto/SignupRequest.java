package ua.hazelcast.cluster.deployment.dto;

import lombok.Data;
import ua.hazelcast.cluster.deployment.entity.UserRole;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class SignupRequest {

    @Email
    private String email;

    @NotBlank
    private String password;

    private String firstName;

    private String lastName;

    @NotEmpty
    private List<UserRole> roles;
}
