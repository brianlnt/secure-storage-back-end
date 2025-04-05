package project.brianle.securestorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import project.brianle.securestorage.domain.Response;
import project.brianle.securestorage.dto.request.*;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.enumeration.AccountInfoProperties;
import project.brianle.securestorage.handler.CustomLogoutHandler;
import project.brianle.securestorage.service.JwtService;
import project.brianle.securestorage.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyMap;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static project.brianle.securestorage.constant.Constants.FILE_STORAGE;
import static project.brianle.securestorage.enumeration.TokenType.ACCESS;
import static project.brianle.securestorage.enumeration.TokenType.REFRESH;
import static project.brianle.securestorage.utils.RequestUtils.getResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/user"})
@Tag(name = "User Management", description = "APIs for managing user operations including registration, authentication, and profile management")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final CustomLogoutHandler logoutHandler;

    @Operation(summary = "Register new user", 
               description = "Creates a new user account and sends verification email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/register")
    public ResponseEntity<Response> saveUser(@RequestBody @Valid UserRequest user, HttpServletRequest request){
        userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
        return ResponseEntity.created(getUri()).body(getResponse(request, emptyMap(), "Account created, Check your email to enable your account.", HttpStatus.CREATED));
    }

    @Operation(summary = "Verify user account", 
               description = "Verifies user account using the verification key sent via email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid verification key")
    })
    @GetMapping("/verify/account")
    public ResponseEntity<Response> verifyAccount(@RequestParam("key") String key, HttpServletRequest request) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        userService.verifyAccount(key);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account verified.", HttpStatus.OK));
    }

    @Operation(summary = "Set up Multi-Factor Authentication", 
               description = "Enables MFA for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "MFA setup successful"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PatchMapping("/mfa/setup")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> setUpMfa(@AuthenticationPrincipal UserResponse userPrincipal, HttpServletRequest request) {
        UserResponse user = userService.setUpMfa(userPrincipal.getId());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "MFA set up successfully", OK));
    }

    @Operation(summary = "Cancel Multi-Factor Authentication", 
               description = "Disables MFA for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "MFA canceled successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PatchMapping("/mfa/cancel")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> cancelMfa(@AuthenticationPrincipal UserResponse userPrincipal, HttpServletRequest request) {
        UserResponse user = userService.cancelMfa(userPrincipal.getId());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "MFA canceled successfully", OK));
    }

    @Operation(summary = "Verify QR Code", 
               description = "Verifies QR code for MFA setup")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "QR code verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid QR code")
    })
    @PostMapping("/verify/qrcode")
    public ResponseEntity<Response> verifyQrcode(@RequestBody QrCodeRequest qrCodeRequest, HttpServletRequest request, HttpServletResponse response) {
        var user = userService.verifyQrCode(qrCodeRequest.getUserId(), qrCodeRequest.getQrCode());
        jwtService.addCookie(response, user, ACCESS);
        jwtService.addCookie(response, user, REFRESH);
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "QR code verified", OK));
    }

    //user info with login
    @Operation(summary = "Get user profile", 
               description = "Retrieves the profile information of the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @GetMapping("/profile")
    @PreAuthorize("hasAnyAuthority('user:read') or hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> profile(@AuthenticationPrincipal UserResponse userPrinciple,  HttpServletRequest request){
        var user = userService.getUserByUserId(userPrinciple.getUserId());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "Profile retrieved", OK));
    }

    @Operation(summary = "Update user information", 
               description = "Updates the profile information of the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PatchMapping("/update")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> update(@AuthenticationPrincipal UserResponse userPrinciple, @RequestBody UserRequest userRequest, HttpServletRequest request){
        var user = userService.updateUser(userPrinciple.getUserId(), userRequest.getFirstName(), userRequest.getLastName(), userRequest.getEmail(), userRequest.getPhone(), userRequest.getBio());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "User updated successfully", OK));
    }

    @Operation(summary = "Update user role", 
               description = "Updates the role of a user (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role updated successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PatchMapping("/updaterole")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> updateRole(@AuthenticationPrincipal UserResponse userPrinciple, @RequestBody RoleRequest roleRequest, HttpServletRequest request){
        userService.updateRole(userPrinciple.getUserId(), roleRequest.getRole());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "User role updated successfully", OK));
    }

    @Operation(summary = "Set account expired status", 
               description = "Sets the account as expired (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account status updated successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PatchMapping("/setaccountexpired")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> setAccountExpired(@AuthenticationPrincipal UserResponse userPrinciple, HttpServletRequest request){
        userService.setAccountInfo(userPrinciple.getUserId(), AccountInfoProperties.EXPIRED);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account updated successfully", OK));
    }

    @Operation(summary = "Set account locked status", 
               description = "Sets the account as locked (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account status updated successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PatchMapping("/setaccountlocked")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> setAccountLocked(@AuthenticationPrincipal UserResponse userPrinciple, HttpServletRequest request){
        userService.setAccountInfo(userPrinciple.getUserId(), AccountInfoProperties.LOCKED);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account updated successfully", OK));
    }

    @Operation(summary = "Set account enabled status", 
               description = "Sets the account as enabled (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account status updated successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PatchMapping("/setaccountenabled")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> setAccountEnabled(@AuthenticationPrincipal UserResponse userPrinciple, HttpServletRequest request){
        userService.setAccountInfo(userPrinciple.getUserId(), AccountInfoProperties.ENABLED);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account updated successfully", OK));
    }

    @Operation(summary = "Set credential expired status", 
               description = "Sets the credential as expired (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Credential status updated successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PatchMapping("/setcredentialexpired")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> setCredentialExpired(@AuthenticationPrincipal UserResponse userPrinciple, HttpServletRequest request){
        userService.setAccountInfo(userPrinciple.getUserId(), AccountInfoProperties.CREDENTIAL_EXPIRED);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account updated successfully", OK));
    }

    @Operation(summary = "Update user password", 
               description = "Updates the password of the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password updated successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PatchMapping("/updatepassword")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> updatePassword(@AuthenticationPrincipal UserResponse userPrinciple, @RequestBody UpdatePasswordRequest updatePasswordRequest, HttpServletRequest request){
        userService.updatePassword(userPrinciple.getUserId(), updatePasswordRequest.getCurrentPassword(), updatePasswordRequest.getNewPassword(), updatePasswordRequest.getConfirmNewPassword());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Password updated successfully", OK));
    }

    //reset password without login
    @Operation(summary = "Reset user password", 
               description = "Sends a password reset email to the user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid email")
    })
    @PostMapping("/resetpassword")
    public ResponseEntity<Response> resetPasswordRequest(@RequestBody EmailRequest emailRequest, HttpServletRequest request){
        userService.resetPassword(emailRequest.getEmail());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "We sent an email to reset your password, please check.", HttpStatus.OK));
    }

    @Operation(summary = "Verify password reset", 
               description = "Verifies the password reset link and allows user to enter new password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid reset key")
    })
    @GetMapping("/verify/password")
    public ResponseEntity<Response> verifyPassword(@RequestParam("key") String key, HttpServletRequest request){
        UserResponse user = userService.verifyPassword(key);
        return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "Enter new password", HttpStatus.OK));
    }

    @Operation(summary = "Reset new password", 
               description = "Resets the user's password with a new password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid reset key")
    })
    @PostMapping("/resetpassword/reset")
    public ResponseEntity<Response> resetNewPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest, HttpServletRequest request){
        userService.updateResetPassword(resetPasswordRequest.getUserId(), resetPasswordRequest.getNewPassword(), resetPasswordRequest.getConfirmNewPassword());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Password reset successfully", HttpStatus.OK));
    }
    //end

    @Operation(summary = "Get all users", 
               description = "Retrieves a list of all users (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @GetMapping()
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> getUsers(@AuthenticationPrincipal UserResponse userPrinciple, HttpServletRequest request){
        return ResponseEntity.ok().body(getResponse(request, Map.of("users", userService.getUsers()), "Users retrieved successfully", OK));
    }

    @Operation(summary = "Upload user profile photo", 
               description = "Uploads or updates the user's profile picture")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Photo uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PatchMapping("/photo")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> uploadPhoto(@AuthenticationPrincipal UserResponse userPrinciple, @RequestParam("file") MultipartFile file, HttpServletRequest request){
        var imageUrl = userService.uploadPhoto(userPrinciple.getUserId(), file);
        return ResponseEntity.ok().body(getResponse(request, of("imageUrl", imageUrl), "Photo uploaded successfully", OK));
    }

    @Operation(summary = "Get user photo", 
               description = "Retrieves user's profile photo by filename")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Photo retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Photo not found")
    })
    @GetMapping(path = "/image/{filename}", produces = { IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE })
    public byte[] getPhoto(@PathVariable("filename") String filename) throws IOException{
        return Files.readAllBytes(Paths.get(FILE_STORAGE + filename));
    }

    @Operation(summary = "Logout user", 
               description = "Logs out the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @PostMapping("/logout")
    public ResponseEntity<Response> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        logoutHandler.logout(request, response, authentication);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "You've logged out successfully", OK));
    }

    private URI getUri() {
        return URI.create("");
    }
}
