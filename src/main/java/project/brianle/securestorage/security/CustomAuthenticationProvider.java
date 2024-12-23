package project.brianle.securestorage.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import project.brianle.securestorage.domain.CustomUserDetails;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.entity.CredentialEntity;
import project.brianle.securestorage.service.UserService;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final UserService userService;
    private final BCryptPasswordEncoder encoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CustomAuthenticationToken authenticationToken = (CustomAuthenticationToken) authentication;
        UserResponse user = userService.getUserByEmail(authenticationToken.getEmail());

        CredentialEntity userCredential = userService.getUserCredentialById(user.getId());
        CustomUserDetails customerUserDetails = new CustomUserDetails(user, userCredential);
        validAccount.accept(customerUserDetails);
        if(encoder.matches(authenticationToken.getPassword(), customerUserDetails.getPassword())){
            return CustomAuthenticationToken.authenticated(user, customerUserDetails.getAuthorities());
        } else {
            throw new BadCredentialsException("Email and/or password incorrect. Please try again");
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private final Consumer<CustomUserDetails> validAccount = userPrincipal -> {
        if(!userPrincipal.isAccountNonLocked()) { throw new LockedException("Your account is currently locked"); }
        if(!userPrincipal.isEnabled()) { throw new DisabledException("Your account is currently disabled"); }
        if(!userPrincipal.isCredentialsNonExpired()) { throw new CredentialsExpiredException("Your password has expired. Please update your password"); }
        if(!userPrincipal.isAccountNonExpired()) { throw new DisabledException("Your account has expired. Please contact administrator"); }
    };
}