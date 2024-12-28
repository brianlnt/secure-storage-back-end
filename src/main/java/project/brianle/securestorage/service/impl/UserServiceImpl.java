package project.brianle.securestorage.service.impl;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import project.brianle.securestorage.cache.CacheStore;
import project.brianle.securestorage.domain.RequestContext;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.entity.ConfirmationEntity;
import project.brianle.securestorage.entity.CredentialEntity;
import project.brianle.securestorage.entity.RoleEntity;
import project.brianle.securestorage.entity.UserEntity;
import project.brianle.securestorage.enumeration.AccountInfoProperties;
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
import project.brianle.securestorage.utils.AccountUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static project.brianle.securestorage.constant.Constants.FILE_STORAGE;
import static project.brianle.securestorage.utils.UserUtils.*;

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
    private final BCryptPasswordEncoder encoder;
    private final ApplicationEventPublisher publisher;

    @Override
    public void createUser(String firstName, String lastName, String email, String password) {
        UserEntity userEntity = userRepository.save(createNewUser(firstName, lastName, email));
        CredentialEntity credentialEntity = new CredentialEntity(encoder.encode(password), userEntity);
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

    @Override
    public UserResponse getUserByUserId(String userId) {
        var userEntity = userRepository.findUserByUserId(userId).orElseThrow(() -> new ApiException("User not found"));
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        UserEntity userEntity = getUserEntityByEmail(email);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public CredentialEntity getUserCredentialById(Long userId) {
        var credentialById = credentialRepository.getCredentialByUserEntityId(userId);
        return credentialById.orElseThrow(() -> new ApiException("Unable to find user credential"));
    }

    @Override
    public UserResponse setUpMfa(Long id) {
        var userEntity = getUserEntityById(id);
        var codeSecret = qrCodeSecret.get();
        userEntity.setQrCodeImageUri(qrCodeImageUri.apply(userEntity.getEmail(), codeSecret));
        userEntity.setQrCodeSecret(codeSecret);
        userEntity.setMfa(true);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public UserResponse cancelMfa(Long id) {
        var userEntity = getUserEntityById(id);
        userEntity.setMfa(false);
        userEntity.setQrCodeSecret(EMPTY);
        userEntity.setQrCodeImageUri(EMPTY);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public UserResponse verifyQrCode(String userId, String qrCode) {
        UserEntity userEntity = getUserEntityByUserId(userId);
        verifyCode(qrCode, userEntity.getQrCodeSecret());
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void resetPassword(String email) {
        UserEntity user = getUserEntityByEmail(email);
        ConfirmationEntity confirmationEntity = getUserConfirmation(user);
        if(confirmationEntity != null) {
            publisher.publishEvent(new UserEvent(user, EventType.RESETPASSWORD, Map.of("key", confirmationEntity.getKey())));
        } else {
            ConfirmationEntity newConfirmationEntity = new ConfirmationEntity(user);
            confirmationRepository.save(newConfirmationEntity);
            publisher.publishEvent(new UserEvent(user, EventType.RESETPASSWORD, Map.of("key", newConfirmationEntity.getKey())));
        }
    }

    @Override
    public UserResponse verifyPassword(String key) {
        ConfirmationEntity confirmationEntity = getUserConfirmation(key);
        UserEntity userEntity = confirmationEntity.getUserEntity();
        AccountUtils.verifyAccountStatus(userEntity);
        confirmationRepository.delete(confirmationEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void updateResetPassword(String userId, String newPassword, String confirmNewPassword) {
        if(!newPassword.equals(confirmNewPassword)) throw new ApiException("Passwords don't match. Please try again.");
        UserEntity userEntity = getUserEntityByUserId(userId);
        CredentialEntity credentialEntity = getUserCredentialById(userEntity.getId());
        credentialEntity.setPassword(encoder.encode(newPassword));
        credentialRepository.save(credentialEntity);
    }

    @Override
    public UserResponse updateUser(String userId, String firstName, String lastName, String email, String phone, String bio) {
        UserEntity userEntity = getUserEntityByUserId(userId);
        userEntity.setFirstName(firstName);
        userEntity.setLastName(lastName);
        userEntity.setEmail(email);
        userEntity.setPhone(phone);
        userEntity.setBio(bio);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void updateRole(String userId, String role) {
        UserEntity userEntity = getUserEntityByUserId(userId);
        userEntity.setRole(getRoleName(role));
        userRepository.save(userEntity);
    }

    @Override
    public void setAccountInfo(String userId, AccountInfoProperties accountInfoProperties) {
        UserEntity userEntity= getUserEntityByUserId(userId);
        switch (accountInfoProperties){
            case EXPIRED -> userEntity.setAccountNonExpired(!userEntity.isAccountNonExpired());
            case LOCKED -> userEntity.setAccountNonLocked(!userEntity.isAccountNonLocked());
            case ENABLED -> userEntity.setEnabled(!userEntity.isEnabled());
            case CREDENTIAL_EXPIRED -> {
                CredentialEntity credential = getUserCredentialById(userEntity.getId());
                credential.setUpdatedAt(LocalDateTime.of(1999,1,1,12,0));
                credentialRepository.save(credential);
            }
            default -> {}
        }
    }

    @Override
    public void updatePassword(String userId, String currentPassword, String newPassword, String confirmNewPassword) {
        if(!newPassword.equals(confirmNewPassword)) throw new ApiException("Password don't match. Please try again.");
        UserEntity userEntity = getUserEntityByUserId(userId);
        AccountUtils.verifyAccountStatus(userEntity);
        CredentialEntity credentialEntity = getUserCredentialById(userEntity.getId());
        if(!encoder.matches(currentPassword, credentialEntity.getPassword())) throw new ApiException("Existing password is incorrect. Please try again.");
        credentialEntity.setPassword(encoder.encode(newPassword));
        credentialRepository.save(credentialEntity);
    }

    @Override
    public String uploadPhoto(String userId, MultipartFile file) {
        UserEntity userEntity = getUserEntityByUserId(userId);
        String photoUrl = photoFunction.apply(userId, file);
        userEntity.setImageUrl(photoUrl);
        userRepository.save(userEntity);
        return photoUrl;
    }

    @Override
    public UserResponse getUserById(Long id) {
        var userEntity = getUserEntityById(id);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    public static BiFunction<String, MultipartFile, String> photoFunction = (id, file) -> {
        var filename = id + ".png";
        try {
            var fileStorageLocation = Paths.get(FILE_STORAGE).toAbsolutePath().normalize();
            if(!Files.exists(fileStorageLocation)) {Files.createDirectories(fileStorageLocation);}
            Files.copy(file.getInputStream(), fileStorageLocation.resolve(filename), REPLACE_EXISTING);
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/user/image/" + filename).toUriString();
        } catch (Exception exception) {
            throw new ApiException("Unable to save image");
        }
    };

    private ConfirmationEntity getUserConfirmation(UserEntity user) {
        return confirmationRepository.findByUserEntity(user).orElse(null);
    }

    private ConfirmationEntity getUserConfirmation(String key) {
        return confirmationRepository.findByKey(key).orElseThrow(() -> new ApiException("Unable to find the key"));
    }

    private boolean verifyCode(String qrCode, String qrCodeSecret) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        if(codeVerifier.isValidCode(qrCodeSecret, qrCode)) {
            return true;
        } else {
            throw new ApiException("Invalid QR code. Please try again.");
        }
    }

    private UserEntity getUserEntityByUserId(String userId) {
        var userByUserId = userRepository.findUserByUserId(userId);
        return userByUserId.orElseThrow(() -> new ApiException("User not found"));
    }

    private UserEntity getUserEntityById(Long id) {
        var userById = userRepository.findById(id);
        return userById.orElseThrow(() -> new ApiException("User not found"));
    }

    private UserEntity getUserEntityByEmail(String email){
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new ApiException("User not found"));
    }

    private UserEntity createNewUser(String firstName, String lastName, String email){
        RoleEntity role = getRoleName(Authority.USER.name());
        return createUserEntity(firstName, lastName, email, role);
    }
}
