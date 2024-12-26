package project.brianle.securestorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.brianle.securestorage.domain.Response;
import project.brianle.securestorage.dto.request.EmailRequest;
import project.brianle.securestorage.dto.request.QrCodeRequest;
import project.brianle.securestorage.dto.request.ResetPasswordRequest;
import project.brianle.securestorage.dto.request.UserRequest;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.security.CustomAuthenticationFilter;
import project.brianle.securestorage.security.CustomAuthenticationToken;
import project.brianle.securestorage.service.JwtService;
import project.brianle.securestorage.service.UserService;

import java.net.URI;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;
import static project.brianle.securestorage.enumeration.TokenType.ACCESS;
import static project.brianle.securestorage.enumeration.TokenType.REFRESH;
import static project.brianle.securestorage.utils.RequestUtils.getResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/user"})
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<Response> saveUser(@RequestBody @Valid UserRequest user, HttpServletRequest request){
        userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());
        return ResponseEntity.created(getUri()).body(getResponse(request, emptyMap(), "Account created, Check your email to enable your account.", HttpStatus.CREATED));
    }

    @GetMapping("/verify/account")
    public ResponseEntity<Response> verifyAccount(@RequestParam("key") String key, HttpServletRequest request){
        userService.verifyAccount(key);
        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account verified.", HttpStatus.OK));
    }

    @PatchMapping("/mfa/setup")
    public ResponseEntity<Response> setUpMfa(@AuthenticationPrincipal UserResponse userPrincipal, HttpServletRequest request) {
        UserResponse user = userService.setUpMfa(userPrincipal.getId());
        return ResponseEntity.ok().body(getResponse(request, of("user", user), "MFA set up successfully", OK));
    }

    @PatchMapping("/mfa/cancel")
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

    private URI getUri() {
        return URI.create("");
    }
}
