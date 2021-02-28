package ua.hazelcast.cluster.deployment.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.hazelcast.cluster.deployment.entity.UserEntity;
import ua.hazelcast.cluster.deployment.repository.UserRepository;

import java.util.Optional;

@Service(value = "userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        Optional<UserEntity> userEntity = userRepository.findOneByEmailIgnoreCase(email);
        if (userEntity.isEmpty()) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        final UserEntity user = userEntity.get();
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
