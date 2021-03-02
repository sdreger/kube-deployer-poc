package ua.hazelcast.cluster.deployment.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@ConstructorBinding
@ConfigurationProperties(prefix = "spring.security.jwt", ignoreUnknownFields = false)
public class SecurityProperties {

    private final Token token;

    @Getter
    @AllArgsConstructor
    @ConstructorBinding
    public static class Token {

        @NotBlank
        private final String secretKey;

        @NotNull
        @Min(1000)
        private final Long expirationTime;
    }
}
