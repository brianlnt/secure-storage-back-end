package project.brianle.securestorage.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import project.brianle.securestorage.entity.ConfirmationEntity;
import project.brianle.securestorage.entity.CredentialEntity;
import project.brianle.securestorage.entity.RoleEntity;
import project.brianle.securestorage.entity.UserEntity;
import project.brianle.securestorage.enumeration.Authority;
import project.brianle.securestorage.enumeration.EventType;
import project.brianle.securestorage.event.UserEvent;
import project.brianle.securestorage.exceptions.ApiException;
import project.brianle.securestorage.repository.ConfirmationRepository;
import project.brianle.securestorage.repository.CredentialRepository;
import project.brianle.securestorage.repository.RoleRepository;
import project.brianle.securestorage.repository.UserRepository;
import project.brianle.securestorage.service.UserService;

import java.util.Map;
import java.util.Optional;

import static project.brianle.securestorage.utils.UserUtils.createUserEntity;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CredentialRepository credentialRepository;
    private final ConfirmationRepository confirmationRepository;
//    private final BCriptPasswordEncoder encoder;
    private final ApplicationEventPublisher publisher;

    @Override
    public void createUser(String firstName, String lastName, String email, String password) {
        UserEntity userEntity = userRepository.save(createNewUser(firstName, lastName, email));
        CredentialEntity credentialEntity = new CredentialEntity(password, userEntity);
        credentialRepository.save(credentialEntity);
        ConfirmationEntity confirmationEntity = new ConfirmationEntity(userEntity);
        confirmationRepository.save(confirmationEntity);
        publisher.publishEvent(new UserEvent(userEntity, EventType.REGISTERATION, Map.of("key", confirmationEntity.getKey())));
    }

    @Override
    public RoleEntity getRoleName(String name) {
        Optional<RoleEntity> role = roleRepository.findByNameIgnoreCase(name);
        return role.orElseThrow(() -> new ApiException("Role not found"));
    }

    @Override
    public void verifyAccount(String key) {
        ConfirmationEntity confirmationEntity = confirmationRepository.findByKey(key).orElseThrow(() -> new ApiException("Key not found."));
        UserEntity userEntity = userRepository.findByEmailIgnoreCase(confirmationEntity.getUserEntity().getEmail()).orElseThrow(() -> new ApiException("User not found"));
        userEntity.setEnabled(true);
        userRepository.save(userEntity);
        confirmationRepository.delete(confirmationEntity);

    }

    private UserEntity createNewUser(String firstName, String lastName, String email){
        RoleEntity role = getRoleName(Authority.USER.name());
        return createUserEntity(firstName, lastName, email, role);
    }
}
