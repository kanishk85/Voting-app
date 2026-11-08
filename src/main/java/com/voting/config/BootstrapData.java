package com.voting.config;

import com.voting.entity.Role;
import com.voting.entity.User;
import com.voting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapData {
    @Bean
    CommandLineRunner seedData(UserRepository users, PasswordEncoder passwordEncoder,
                               @Value("${app.admin.name}") String adminName,
                               @Value("${app.admin.email}") String adminEmail,
                               @Value("${app.admin.password}") String adminPassword) {
        return args -> {
            if (adminPassword == null || adminPassword.length() < 8) {
                throw new IllegalStateException("APP_ADMIN_PASSWORD must contain at least 8 characters.");
            }
            if (!users.existsByEmailIgnoreCase(adminEmail)) {
                users.save(new User(adminName, adminEmail.trim().toLowerCase(java.util.Locale.ROOT),
                        passwordEncoder.encode(adminPassword), "0000000000", Role.ROLE_ADMIN));
            }
        };
    }
}
