package project.brianle.securestorage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.brianle.securestorage.domain.Response;
import project.brianle.securestorage.dto.request.UserRequest;
import project.brianle.securestorage.service.UserService;

import java.net.URI;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static project.brianle.securestorage.utils.RequestUtils.getResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = {"/user"})
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequest user){
        UsernamePasswordAuthenticationToken unauthenticated = UsernamePasswordAuthenticationToken.unauthenticated(user.getFirstName(), user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(unauthenticated);
        return ResponseEntity.ok().body(Map.of("user", authenticate));
    }

    private URI getUri() {
        return URI.create("");
    }
}
