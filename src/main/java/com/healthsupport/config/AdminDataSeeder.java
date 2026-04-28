package com.healthsupport.config;

import com.healthsupport.model.Role;
import com.healthsupport.model.User;
import com.healthsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminDataSeeder implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "admin@888";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        userRepository.findByEmail(ADMIN_EMAIL.toLowerCase()).ifPresentOrElse(existing -> {
            existing.setName("System Admin");
            existing.setRole(Role.ADMIN);
            existing.setApproved(true);
            existing.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            userRepository.save(existing);
        }, () -> {
            User admin = User.builder()
                    .name("System Admin")
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .role(Role.ADMIN)
                    .approved(true)
                    .build();
            userRepository.save(admin);
        });
    }
}