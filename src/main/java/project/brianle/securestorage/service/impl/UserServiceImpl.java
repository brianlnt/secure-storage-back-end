package project.brianle.securestorage.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import project.brianle.securestorage.cache.CacheStore;
import project.brianle.securestorage.domain.RequestContext;
import project.brianle.securestorage.entity.ConfirmationEntity;
import project.brianle.securestorage.entity.CredentialEntity;
import project.brianle.securestorage.entity.RoleEntity;
import project.brianle.securestorage.entity.UserEntity;
import project.brianle.securestorage.enumeration.Authority;
import project.brianle.securestorage.enumeration.EventType;
import project.brianle.securestorage.enumeration.LoginType;
import project.brianle.securestorage.event.UserEvent;
import project.brianle.securestorage.exceptions.ApiException;
import project.brianle.securestorage.repository.ConfirmationRepository;
import project.brianle.securestorage.repository.CredentialRepository;
import project.brianle.securestorage.repository.RoleRepository;
import project.brianle.securestorage.repository.UserRepository;
import project.brianle.securestorage.service.UserService;

import java.time.LocalDateTime;
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
    private final CacheStore<String, Integer> cacheStore;
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
        UserEntity userEntity = getUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
        userEntity.setEnabled(true);
        userRepository.save(userEntity);
        confirmationRepository.delete(confirmationEntity);

    }

    @Override
    public void updateLoginAttempt(String email, LoginType loginType) {
        UserEntity userEntity = getUserEntityByEmail(email);
        RequestContext.setUserId(userEntity.getId());
        switch (loginType){
            case LOGIN_ATTEMPT -> {
                if(cacheStore.get(userEntity.getEmail()) == null){
                   userEntity.setLoginAttempts(0);
                   userEntity.setAccountNonLocked(true);
                }
                userEntity.setLoginAttempts(userEntity.getLoginAttempts() + 1);
                cacheStore.put(userEntity.getEmail(), userEntity.getLoginAttempts());
                if(cacheStore.get(userEntity.getEmail()) > 5){
                    userEntity.setAccountNonLocked(false);
                }
            }
            case LOGIN_SUCCESS -> {
                userEntity.setAccountNonLocked(true);
                userEntity.setLoginAttempts(0);
                userEntity.setLastLogin(LocalDateTime.now());
                cacheStore.evict(userEntity.getEmail());
            }
        }
        userRepository.save(userEntity);
    }

    private UserEntity getUserEntityByEmail(String email){
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new ApiException("User not found"));
    }

    private UserEntity createNewUser(String firstName, String lastName, String email){
        RoleEntity role = getRoleName(Authority.USER.name());
        return createUserEntity(firstName, lastName, email, role);
    }
}
