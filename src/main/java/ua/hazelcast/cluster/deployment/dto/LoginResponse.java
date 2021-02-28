package ua.hazelcast.cluster.deployment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    @JsonProperty("email")
    String email;

    @JsonProperty("access_token")
    String accessToken;
}
