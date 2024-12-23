package project.brianle.securestorage.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import project.brianle.securestorage.domain.RequestContext;
import project.brianle.securestorage.domain.Token;
import project.brianle.securestorage.domain.TokenData;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.enumeration.TokenType;
import project.brianle.securestorage.service.JwtService;
import project.brianle.securestorage.utils.RequestUtils;

import java.io.IOException;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpMethod.OPTIONS;
import static project.brianle.securestorage.constant.Constants.PUBLIC_ROUTES;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Optional<String> accessToken = jwtService.extractToken(request, TokenType.ACCESS.getValue());
            if(accessToken.isPresent() && jwtService.getTokenData(accessToken.get(), TokenData::isValid)){
                SecurityContextHolder.getContext().setAuthentication(getAuthentication(accessToken.get(), request));
                RequestContext.setUserId(jwtService.getTokenData(accessToken.get(), TokenData::getUser).getId());
            } else {
                Optional<String> refreshToken = jwtService.extractToken(request, TokenType.REFRESH.getValue());
                if(refreshToken.isPresent() && jwtService.getTokenData(refreshToken.get(), TokenData::isValid)) {
                    UserResponse user = jwtService.getTokenData(refreshToken.get(), TokenData::getUser);
                    SecurityContextHolder.getContext().setAuthentication(getAuthentication(jwtService.createToken(user, Token::getAccess), request));
                    jwtService.addCookie(response, user, TokenType.ACCESS);
                    RequestContext.setUserId(user.getId());
                } else {
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception exception){
            log.info(exception.getMessage());
            RequestUtils.handleErrorResponse(request,response,exception);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        boolean shouldNotFilter = request.getMethod().equalsIgnoreCase(OPTIONS.name()) || asList(PUBLIC_ROUTES).contains(request.getRequestURI());
        if(shouldNotFilter) { RequestContext.setUserId(0L); }
        return shouldNotFilter;
    }

    private Authentication getAuthentication(String token, HttpServletRequest request) {
        CustomAuthenticationToken authentication = CustomAuthenticationToken.authenticated(jwtService.getTokenData(token, TokenData::getUser), jwtService.getTokenData(token, TokenData::getAuthorities));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }
}
