package com.vault.auth.config;

import com.vault.auth.model.User;
import com.vault.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Value;

@Configuration
public class DataSeeder {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner loadData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                System.out.println("Seeding secure admin user...");
                User admin = new User(adminUsername, passwordEncoder.encode(adminPassword));
                userRepository.save(admin);
            }
        };
    }
}
