package project.brianle.securestorage.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import project.brianle.securestorage.domain.Token;
import project.brianle.securestorage.domain.TokenData;
import project.brianle.securestorage.dto.response.UserResponse;
import project.brianle.securestorage.enumeration.TokenType;

import java.util.Optional;
import java.util.function.Function;

public interface JwtService {
    String createToken(UserResponse user, Function<Token, String> tokenFunction);
    Optional<String> extractToken(HttpServletRequest request, String cookieName);
    void addCookie(HttpServletResponse response, UserResponse user, TokenType tokenType);
    <T> T getTokenData(String token, Function<TokenData, T> tokenFunction);
    void removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName);
}
