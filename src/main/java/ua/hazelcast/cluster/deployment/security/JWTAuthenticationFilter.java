package ua.hazelcast.cluster.deployment.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ua.hazelcast.cluster.deployment.dto.LoginRequest;
import ua.hazelcast.cluster.deployment.dto.LoginResponse;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

/**
 * Filter that checks if the provided credentials are valid or not, and in case of success - issues a new token.
 */
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final Long expirationTime;

    private final String secretKey;

    private final AuthenticationManager authenticationManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JWTAuthenticationFilter(final AuthenticationManager authenticationManager,
                                   final Long expirationTime, final String secretKey) {
        this.expirationTime = expirationTime;
        this.secretKey = secretKey;
        this.authenticationManager = authenticationManager;
        setFilterProcessesUrl("/api/user/login");
    }
    @Override
    public Authentication attemptAuthentication(final HttpServletRequest req,
                                                final HttpServletResponse res) throws AuthenticationException {
        try {
            final LoginRequest credentials = objectMapper.readValue(req.getInputStream(), LoginRequest.class);
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.getEmail(),
                            credentials.getPassword(),
                            Collections.emptyList())
            );
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void successfulAuthentication(final HttpServletRequest req, final HttpServletResponse res,
                                            final FilterChain chain, final Authentication auth) throws IOException {
        final long expirationTimestamp = System.currentTimeMillis() + expirationTime;
        final String username = ((User) auth.getPrincipal()).getUsername();
        final String token = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(expirationTimestamp))
                .sign(Algorithm.HMAC512(secretKey.getBytes()));
        final LoginResponse response = new LoginResponse(username, token, expirationTimestamp);
        res.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(objectMapper.writeValueAsString(response));
        res.getWriter().flush();
    }
}
