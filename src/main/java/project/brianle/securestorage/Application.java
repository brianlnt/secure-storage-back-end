package project.brianle.securestorage;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import project.brianle.securestorage.domain.RequestContext;
import project.brianle.securestorage.entity.RoleEntity;
import project.brianle.securestorage.enumeration.Authority;
import project.brianle.securestorage.repository.RoleRepository;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(RoleRepository roleRepository) {
        return args -> {
//			RequestContext.setUserId(0L);
//			RoleEntity userRole = new RoleEntity();
//			userRole.setName(Authority.USER.name());
//			userRole.setAuthorities(Authority.USER.getValue());
//			roleRepository.save(userRole);
//
//            RoleEntity adminRole = new RoleEntity();
//			adminRole.setName(Authority.ADMIN.name());
//			adminRole.setAuthorities(Authority.ADMIN.getValue());
//			roleRepository.save(adminRole);
//			RequestContext.start();
        };
    }
}
