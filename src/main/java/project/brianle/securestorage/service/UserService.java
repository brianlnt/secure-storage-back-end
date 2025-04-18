package project.brianle.securestorage.service;

import org.springframework.web.multipart.MultipartFile;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.entity.CredentialEntity;
import project.brianle.securestorage.entity.RoleEntity;
import project.brianle.securestorage.enumeration.AccountInfoProperties;
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

    void resetPassword(String email);

    UserResponse verifyPassword(String key);

    void updateResetPassword(String userId, String newPassword, String confirmNewPassword);

    UserResponse updateUser(String userId, String firstName, String lastName, String email, String phone, String bio);

    void updateRole(String userId, String role);

    void setAccountInfo(String userId, AccountInfoProperties accountInfoProperties);

    void updatePassword(String userId, String currentPassword, String newPassword, String confirmNewPassword);

    String uploadPhoto(String userId, MultipartFile file);

    UserResponse getUserById(Long id);

    UserResponse getUsers();
}