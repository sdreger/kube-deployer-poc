package ua.hazelcast.cluster.deployment.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;
import ua.hazelcast.cluster.deployment.security.JWTAuthenticationFilter;
import ua.hazelcast.cluster.deployment.security.JWTAuthorizationFilter;

@Configuration
@EnableWebSecurity
@Import(SecurityProblemSupport.class)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;

    private final SecurityProblemSupport problemSupport;

    private final Long expirationTime;

    private final String secretKey;

    @Autowired
    public WebSecurityConfig(final UserDetailsService userDetailsService,
                             final SecurityProblemSupport problemSupport,
                             final @Value("${spring.security.jwt.token.expiration-time}") Long expirationTime,
                             final @Value("${spring.security.jwt.token.secret-key}") String secretKey) {
        this.userDetailsService = userDetailsService;
        this.problemSupport = problemSupport;
        this.expirationTime = expirationTime;
        this.secretKey = secretKey;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {

        http.exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport);

        http.cors()
                .and().authorizeRequests()
                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .antMatchers(HttpMethod.POST, "/api/user/signup").permitAll()
                .antMatchers(HttpMethod.POST, "/api/user/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(new JWTAuthenticationFilter(authenticationManager(), expirationTime, secretKey))
                .addFilter(new JWTAuthorizationFilter(authenticationManager(), secretKey))
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    public void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
}
