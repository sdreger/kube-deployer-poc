package ua.hazelcast.cluster.deployment;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import ua.hazelcast.cluster.deployment.dto.LoginResponse;
import ua.hazelcast.cluster.deployment.dto.SignupRequest;
import ua.hazelcast.cluster.deployment.entity.UserEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends AbstractControllerTest {

    protected static final String TEST_CREATION_USER_EMAIL = "test.creation@test.com";

    // TODO: Add more tests for user credentials (like password length, email correctness, etc.)

    @Test
    public void shouldCreateDeployment() throws Exception {
        final SignupRequest request = new SignupRequest();
        request.setEmail(TEST_CREATION_USER_EMAIL);
        request.setPassword(TEST_USER_PASSWORD);
        request.setFirstName(TEST_USER_FIRST_NAME);
        request.setLastName(TEST_USER_LAST_NAME);
        request.setRoles(TEST_USER_ROLES);

        this.mockMvc.perform(post(URL_USERS + "/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());

        final Optional<UserEntity> testUser = userRepository.findOneByEmailIgnoreCase(request.getEmail());
        assertThat(testUser).isPresent();
        final UserEntity userEntity = testUser.get();
        assertThat(userEntity.getEmail()).isEqualTo(request.getEmail());
        assertThat(userEntity.getPassword()).isNotEqualTo(request.getPassword()); // It should be hashed
        assertThat(userEntity.getFirstName()).isEqualTo(request.getFirstName());
        assertThat(userEntity.getLastName()).isEqualTo(request.getLastName());
        assertThat(userEntity.getRoles()).hasSize(request.getRoles().size());

        userRepository.delete(userEntity);
    }

    @Test
    public void shouldObtainAccessToken() throws Exception {

        final LoginResponse loginResponse = obtainAccessToken(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getEmail()).isEqualTo(TEST_USER_EMAIL);
        assertThat(loginResponse.getAccessToken()).isNotBlank();
        assertThat(loginResponse.getExpires()).isNotNull();
    }
}
