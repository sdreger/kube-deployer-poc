package ua.hazelcast.cluster.deployment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hazelcast.cluster.deployment.dto.SignupRequest;
import ua.hazelcast.cluster.deployment.entity.UserEntity;
import ua.hazelcast.cluster.deployment.mapper.UserMapper;
import ua.hazelcast.cluster.deployment.repository.UserRepository;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(final UserRepository userRepository, final UserMapper userMapper,
                           final BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createUser(final SignupRequest request) {

        final UserEntity userEntity = userMapper.toUserEntity(request);
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userRepository.save(userEntity);
    }
}
