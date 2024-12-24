package project.brianle.securestorage.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import project.brianle.securestorage.domain.Response;
import project.brianle.securestorage.dto.request.LoginRequest;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.enumeration.LoginType;
import project.brianle.securestorage.service.JwtService;
import project.brianle.securestorage.service.UserService;
import project.brianle.securestorage.utils.RequestUtils;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static project.brianle.securestorage.enumeration.LoginType.LOGIN_SUCCESS;
import static project.brianle.securestorage.enumeration.TokenType.ACCESS;
import static project.brianle.securestorage.enumeration.TokenType.REFRESH;
import static project.brianle.securestorage.utils.RequestUtils.getResponse;

@Slf4j
public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final UserService userService;
    private final JwtService jwtService;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService){
        super(new AntPathRequestMatcher("/user/login", "POST"), authenticationManager);
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        try {
            LoginRequest user = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, true).readValue(request.getInputStream(), LoginRequest.class);
            userService.updateLoginAttempt(user.getEmail(), LoginType.LOGIN_ATTEMPT);
            CustomAuthenticationToken authentication = CustomAuthenticationToken.unauthenticated(user.getEmail(), user.getPassword());
            return this.getAuthenticationManager().authenticate(authentication);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            RequestUtils.handleErrorResponse(request, response, exception);
            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        var user = (UserResponse) authentication.getPrincipal();
        userService.updateLoginAttempt(user.getEmail(), LOGIN_SUCCESS);
        var httpResponse = user.isMfa() ? sendQrCode(request, user) : sendResponse(request, response, user);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(OK.value());
        var out = response.getOutputStream();
        var mapper = new ObjectMapper();
        mapper.writeValue(out, httpResponse);
        out.flush();
    }

    private Response sendResponse(HttpServletRequest request, HttpServletResponse response, UserResponse user) {
        jwtService.addCookie(response, user, ACCESS);
        jwtService.addCookie(response, user, REFRESH);
        return getResponse(request, of("user", user), "Login Success", OK);
    }

    private Response sendQrCode(HttpServletRequest request, UserResponse user) {
        return getResponse(request, of("user", user), "Please enter QR code", OK);
    }
}
