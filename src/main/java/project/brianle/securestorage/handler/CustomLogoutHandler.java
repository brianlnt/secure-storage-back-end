package project.brianle.securestorage.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import project.brianle.securestorage.service.JwtService;

import static project.brianle.securestorage.enumeration.TokenType.ACCESS;
import static project.brianle.securestorage.enumeration.TokenType.REFRESH;

@Service
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final JwtService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, authentication);
        jwtService.removeCookie(request, response, ACCESS.getValue());
        jwtService.removeCookie(request, response, REFRESH.getValue());
    }
}
