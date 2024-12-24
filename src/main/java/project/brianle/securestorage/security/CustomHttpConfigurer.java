package project.brianle.securestorage.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import project.brianle.securestorage.service.JwtService;
import project.brianle.securestorage.service.UserService;

@Component
@RequiredArgsConstructor
public class CustomHttpConfigurer extends AbstractHttpConfigurer<CustomHttpConfigurer, HttpSecurity> {
    private final CustomAuthorizationFilter customAuthorizationFilter;
    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Override
    public void init(HttpSecurity http) throws Exception {
        http.authenticationProvider(customAuthenticationProvider);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        //This filter likely handles authorization logic, such as checking JWT tokens.
        http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        //This filter handles authentication logic, such as processing login requests.
        http.addFilterAfter(new CustomAuthenticationFilter(authenticationConfiguration.getAuthenticationManager(), userService, jwtService), UsernamePasswordAuthenticationFilter.class);
    }
}