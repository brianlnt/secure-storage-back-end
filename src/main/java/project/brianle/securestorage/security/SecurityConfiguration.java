package project.brianle.securestorage.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static project.brianle.securestorage.constant.Constants.STRENGTH;

@Configuration
public class SecurityConfiguration {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(STRENGTH);
    }
}
