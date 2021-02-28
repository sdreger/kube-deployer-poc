package ua.hazelcast.cluster.deployment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ua.hazelcast.cluster.deployment.dto.LoginRequest;
import ua.hazelcast.cluster.deployment.dto.LoginResponse;
import ua.hazelcast.cluster.deployment.entity.UserEntity;
import ua.hazelcast.cluster.deployment.entity.UserRole;
import ua.hazelcast.cluster.deployment.repository.UserRepository;

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AbstractControllerTest extends AbstractClusterTest {

    protected static final String URL_USERS = "/api/user";

    protected static final String TEST_USER_EMAIL = "test@test.com";

    protected static final String TEST_USER_PASSWORD = "test";

    protected static final String TEST_USER_FIRST_NAME = "Test FN";

    protected static final String TEST_USER_LAST_NAME = "Test LN";

    protected static final List<UserRole> TEST_USER_ROLES = List.of(UserRole.ROLE_USER);

    protected static final String TOKEN_PREFIX = "Bearer ";

    @Autowired
    protected WebApplicationContext context;

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    protected UserEntity user1;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(springSecurity()).build();

        user1 = new UserEntity();
        user1.setEmail(TEST_USER_EMAIL);
        user1.setPassword(passwordEncoder.encode(TEST_USER_PASSWORD));
        user1.setFirstName(TEST_USER_FIRST_NAME);
        user1.setLastName(TEST_USER_LAST_NAME);
        user1.setRoles(TEST_USER_ROLES);
        userRepository.save(user1);
    }

    @AfterEach
    public void shutdown() {
        userRepository.delete(user1);
    }

    protected String getAccessTokenForUser1() throws Exception {
        return TOKEN_PREFIX + obtainAccessToken(TEST_USER_EMAIL, TEST_USER_PASSWORD).getAccessToken();
    }

    protected LoginResponse obtainAccessToken(String email, String password) throws Exception {

        final LoginRequest request = new LoginRequest(email, password);
        MvcResult result
                = mockMvc.perform(post(URL_USERS + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
    }
}
