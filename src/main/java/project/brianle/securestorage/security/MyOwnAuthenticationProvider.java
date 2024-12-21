package project.brianle.securestorage.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MyOwnAuthenticationProvider implements AuthenticationProvider {
    private final UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String password = (String)authentication.getCredentials();
        UserDetails userDB = userDetailsService.loadUserByUsername((String)authentication.getPrincipal());
        String passwordDB = userDB.getPassword();
        if(password.equals(passwordDB)){
            return UsernamePasswordAuthenticationToken.authenticated(userDB, "[PASSWORD PROTECTED]", null);
        }
        throw new BadCredentialsException("User not found");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
