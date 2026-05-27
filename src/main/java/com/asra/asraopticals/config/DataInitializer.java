package com.asra.asraopticals.config;

import com.asra.asraopticals.model.User;
import com.asra.asraopticals.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Only create admin if it doesn't exist yet
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("zahedali00830@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ROLE_ADMIN");
            admin.setFullname("Asra Admin");
            admin.setVerified(true);
            userRepository.save(admin);
            System.out.println("✅ Admin account created — PLEASE CHANGE PASSWORD after first login!");
        } else {
            System.out.println("ℹ Admin already exists, skipping creation.");
        }
    }
}