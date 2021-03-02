package ua.hazelcast.cluster.deployment.config;

import org.springframework.beans.factory.annotation.Autowired;
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

    private final SecurityProperties securityProperties;

    @Autowired
    public WebSecurityConfig(final UserDetailsService userDetailsService,
                             final SecurityProblemSupport problemSupport,
                             SecurityProperties securityProperties) {
        this.userDetailsService = userDetailsService;
        this.problemSupport = problemSupport;
        this.securityProperties = securityProperties;
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

        final JWTAuthenticationFilter jwtAuthenticationFilter = new JWTAuthenticationFilter(authenticationManager(),
                securityProperties.getToken().getExpirationTime(), securityProperties.getToken().getSecretKey());
        final JWTAuthorizationFilter jwtAuthorizationFilter = new JWTAuthorizationFilter(authenticationManager(),
                securityProperties.getToken().getSecretKey());

        http.cors()
                .and().authorizeRequests()
                .antMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .antMatchers(HttpMethod.POST, "/api/user/signup").permitAll()
                .antMatchers(HttpMethod.POST, "/api/user/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(jwtAuthenticationFilter)
                .addFilter(jwtAuthorizationFilter)
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    public void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
}
