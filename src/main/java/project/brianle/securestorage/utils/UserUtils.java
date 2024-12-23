package project.brianle.securestorage.utils;

import org.springframework.beans.BeanUtils;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.entity.CredentialEntity;
import project.brianle.securestorage.entity.RoleEntity;
import project.brianle.securestorage.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static project.brianle.securestorage.constant.Constants.NINETY_DAYS;

public class UserUtils {
    public static UserEntity createUserEntity(String firstName, String lastName, String email, RoleEntity role){
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .lastLogin(LocalDateTime.now())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .enabled(false)
                .mfa(false)
                .loginAttempts(0)
                .qrCodeSecret("")
                .phone("")
                .bio("")
                .imageUrl("https://cdn-icons-png.flaticon.com/512/149/149071.png")
                .role(role)
                .build();
    }

    public static UserResponse fromUserEntity(UserEntity userEntity, RoleEntity role, CredentialEntity credentialEntity) {
        UserResponse user = new UserResponse();
        BeanUtils.copyProperties(userEntity, user);
        user.setLastLogin(userEntity.getLastLogin().toString());
        user.setCredentialsNonExpired(isCredentialsNonExpired(credentialEntity));
        user.setCreatedAt(userEntity.getCreatedAt().toString());
        user.setUpdatedAt(userEntity.getUpdatedAt().toString());
        user.setRole(role.getName());
        user.setAuthorities(role.getAuthorities().getValue());
        return user;
    }

    public static boolean isCredentialsNonExpired(CredentialEntity credentialEntity) {
        return credentialEntity.getUpdatedAt().plusDays(NINETY_DAYS).isAfter(now());
    }
}
