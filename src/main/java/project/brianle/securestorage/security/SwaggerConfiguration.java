package project.brianle.securestorage.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Secure Storage API")
                        .description("API Documentation for Secure Storage Application")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Brian Le")
                                .email("brianle.lnt@gmail.com")));
    }
}
