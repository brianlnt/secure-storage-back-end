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
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final CustomLogoutHandler logoutHandler;

    @PostMapping("/register")
    public ResponseEntity<Response> saveUser(@RequestBody @Valid UserRequest user, HttpServletRequest request){
        userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
        return ResponseEntity.created(getUri()).body(getResponse(request, emptyMap(), "Account created, Check your email to enable your account.", HttpStatus.CREATED));
    }

    @GetMapping("/verify/account")
    public ResponseEntity<Response> verifyAccount(@RequestParam("key") String key, HttpServletRequest request) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        userService.verifyAccount(key);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account verified.", HttpStatus.OK));
    }

    @PatchMapping("/mfa/setup")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> setUpMfa(@AuthenticationPrincipal UserResponse userPrincipal, HttpServletRequest request) {
        UserResponse user = userService.setUpMfa(userPrincipal.getId());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "MFA set up successfully", OK));
    }

    @PatchMapping("/mfa/cancel")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> cancelMfa(@AuthenticationPrincipal UserResponse userPrincipal, HttpServletRequest request) {
        UserResponse user = userService.cancelMfa(userPrincipal.getId());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "MFA canceled successfully", OK));
    }

    @PostMapping("/verify/qrcode")
    public ResponseEntity<Response> verifyQrcode(@RequestBody QrCodeRequest qrCodeRequest, HttpServletRequest request, HttpServletResponse response) {
        var user = userService.verifyQrCode(qrCodeRequest.getUserId(), qrCodeRequest.getQrCode());
        jwtService.addCookie(response, user, ACCESS);
        jwtService.addCookie(response, user, REFRESH);
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "QR code verified", OK));
    }

    //user info with login
    @GetMapping("/profile")
    @PreAuthorize("hasAnyAuthority('user:read') or hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> profile(@AuthenticationPrincipal UserResponse userPrinciple,  HttpServletRequest request){
        var user = userService.getUserByUserId(userPrinciple.getUserId());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "Profile retrieved", OK));
    }

    @PatchMapping("/update")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> update(@AuthenticationPrincipal UserResponse userPrinciple, @RequestBody UserRequest userRequest, HttpServletRequest request){
        var user = userService.updateUser(userPrinciple.getUserId(), userRequest.getFirstName(), userRequest.getLastName(), userRequest.getEmail(), userRequest.getPhone(), userRequest.getBio());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "User updated successfully", OK));
    }

    @PatchMapping("/updaterole")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> updateRole(@AuthenticationPrincipal UserResponse userPrinciple, @RequestBody RoleRequest roleRequest, HttpServletRequest request){
        userService.updateRole(userPrinciple.getUserId(), roleRequest.getRole());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "User role updated successfully", OK));
    }

    @PatchMapping("/setaccountexpired")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> setAccountExpired(@AuthenticationPrincipal UserResponse userPrinciple, HttpServletRequest request){
        userService.setAccountInfo(userPrinciple.getUserId(), AccountInfoProperties.EXPIRED);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account updated successfully", OK));
    }

    @PatchMapping("/setaccountlocked")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> setAccountLocked(@AuthenticationPrincipal UserResponse userPrinciple, HttpServletRequest request){
        userService.setAccountInfo(userPrinciple.getUserId(), AccountInfoProperties.LOCKED);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account updated successfully", OK));
    }

    @PatchMapping("/setaccountenabled")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> setAccountEnabled(@AuthenticationPrincipal UserResponse userPrinciple, HttpServletRequest request){
        userService.setAccountInfo(userPrinciple.getUserId(), AccountInfoProperties.ENABLED);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account updated successfully", OK));
    }

    @PatchMapping("/setcredentialexpired")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> setCredentialExpired(@AuthenticationPrincipal UserResponse userPrinciple, HttpServletRequest request){
        userService.setAccountInfo(userPrinciple.getUserId(), AccountInfoProperties.CREDENTIAL_EXPIRED);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account updated successfully", OK));
    }

    @PatchMapping("/updatepassword")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> updatePassword(@AuthenticationPrincipal UserResponse userPrinciple, @RequestBody UpdatePasswordRequest updatePasswordRequest, HttpServletRequest request){
        userService.updatePassword(userPrinciple.getUserId(), updatePasswordRequest.getCurrentPassword(), updatePasswordRequest.getNewPassword(), updatePasswordRequest.getConfirmNewPassword());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Password updated successfully", OK));
    }

    //reset password without login
    @PostMapping("/resetpassword")
    public ResponseEntity<Response> resetPasswordRequest(@RequestBody EmailRequest emailRequest, HttpServletRequest request){
        userService.resetPassword(emailRequest.getEmail());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "We sent an email to reset your password, please check.", HttpStatus.OK));
    }

    @GetMapping("/verify/password")
    public ResponseEntity<Response> verifyPassword(@RequestParam("key") String key, HttpServletRequest request){
        UserResponse user = userService.verifyPassword(key);
        return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "Enter new password", HttpStatus.OK));
    }

    @PostMapping("/resetpassword/reset")
    public ResponseEntity<Response> resetNewPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest, HttpServletRequest request){
        userService.updateResetPassword(resetPasswordRequest.getUserId(), resetPasswordRequest.getNewPassword(), resetPasswordRequest.getConfirmNewPassword());
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Password reset successfully", HttpStatus.OK));
    }
    //end

    @GetMapping()
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> getUsers(@AuthenticationPrincipal UserResponse userPrinciple, HttpServletRequest request){
        return ResponseEntity.ok().body(getResponse(request, Map.of("users", userService.getUsers()), "Users retrieved successfully", OK));
    }

    @PatchMapping("/photo")
    @PreAuthorize("hasAnyAuthority('user:update') or hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Response> uploadPhoto(@AuthenticationPrincipal UserResponse userPrinciple, @RequestParam("file") MultipartFile file, HttpServletRequest request){
        var imageUrl = userService.uploadPhoto(userPrinciple.getUserId(), file);
        return ResponseEntity.ok().body(getResponse(request, of("imageUrl", imageUrl), "Photo uploaded successfully", OK));
    }

    @GetMapping(path = "/image/{filename}", produces = { IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE })
    public byte[] getPhoto(@PathVariable("filename") String filename) throws IOException{
        return Files.readAllBytes(Paths.get(FILE_STORAGE + filename));
    }

    @PostMapping("/logout")
    public ResponseEntity<Response> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication){
        logoutHandler.logout(request, response, authentication);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "You've logged out successfully", OK));
    }

    private URI getUri() {
        return URI.create("");
    }
}
