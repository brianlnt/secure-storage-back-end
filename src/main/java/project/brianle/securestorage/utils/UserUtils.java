package project.brianle.securestorage.utils;

import project.brianle.securestorage.entity.RoleEntity;
import project.brianle.securestorage.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.UUID;

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
}
