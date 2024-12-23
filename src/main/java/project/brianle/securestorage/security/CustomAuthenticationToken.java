package project.brianle.securestorage.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.exceptions.ApiException;

import java.util.Collection;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {
    private static final String EMAIL_PROTECTED = "[EMAIL PROTECTED]";
    private static final String PASSWORD_PROTECTED = "PASSWORD_PROTECTED";
    private UserResponse user;
    private String password;
    private String email;
    private boolean authenticated;

    private CustomAuthenticationToken(String email, String password) {
        super((Collection) null);
        this.email = email;
        this.password = password;
        this.authenticated = false;
    }

    private CustomAuthenticationToken(UserResponse user, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.user = user;
        this.email = EMAIL_PROTECTED;
        this.password = PASSWORD_PROTECTED;
        this.authenticated = true;
    }

    public static CustomAuthenticationToken unauthenticated(String username, String password){
        return new CustomAuthenticationToken(username, password);
    }

    public static CustomAuthenticationToken authenticated(UserResponse user, Collection<? extends GrantedAuthority> authorities){
        return new CustomAuthenticationToken(user, authorities);
    }

    @Override
    public Object getCredentials() {return PASSWORD_PROTECTED;}

    @Override
    public Object getPrincipal() {return this.user;}

    @Override
    public void setAuthenticated(boolean authenticated) { throw new ApiException("You cannot set authentication");}

    @Override
    public boolean isAuthenticated() {return this.authenticated;}

    public String getPassword(){return this.password;}

    public String getEmail(){return this.email;}
}
