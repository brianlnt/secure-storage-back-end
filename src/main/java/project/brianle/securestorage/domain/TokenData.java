package project.brianle.securestorage.domain;

import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import project.brianle.securestorage.dto.response.UserResponse;

import java.util.List;

@Builder
@Getter
@Setter
public class TokenData {
    private UserResponse user;
    private Claims claims;
    private boolean valid;
    private List<GrantedAuthority> authorities;
}
