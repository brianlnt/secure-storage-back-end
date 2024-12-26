package project.brianle.securestorage.service;

import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.entity.CredentialEntity;
import project.brianle.securestorage.entity.RoleEntity;
import project.brianle.securestorage.enumeration.LoginType;

public interface UserService {
    void createUser(String firstName, String lastName, String email, String password);
    RoleEntity getRoleName(String name);
    void verifyAccount(String key);
    void updateLoginAttempt(String email, LoginType loginType);
    UserResponse getUserByUserId(String userId);
    UserResponse getUserByEmail(String email);
    CredentialEntity getUserCredentialById(Long userId);

    UserResponse setUpMfa(Long id);
    UserResponse cancelMfa(Long id);

    UserResponse verifyQrCode(String userId, String qrCode);
}