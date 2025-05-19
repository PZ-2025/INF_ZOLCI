package com.example.backend.config;

import com.example.backend.models.User;
import com.example.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

/**
 * Konfiguracja inicjalizacji użytkownika administracyjnego.
 * <p>
 * Klasa odpowiedzialna za sprawdzanie i tworzenie użytkownika administracyjnego
 * przy każdym uruchomieniu aplikacji, jeśli taki użytkownik nie istnieje w bazie danych.
 * </p>
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class AdminInitializerConfig {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializerConfig.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AdminInitializerConfig(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Bean CommandLineRunner, który sprawdza i tworzy użytkownika administracyjnego
     * przy każdym uruchomieniu aplikacji.
     *
     * @return CommandLineRunner, który wykonuje operację sprawdzania i tworzenia admina
     */
    @Bean
    public CommandLineRunner initializeAdminUser() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                logger.info("Użytkownik administracyjny nie istnieje. Tworzenie nowego użytkownika admin...");

                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setEmail("admin@example.com");
                adminUser.setFirstName("System");
                adminUser.setLastName("Administrator");
                adminUser.setRole("administrator");
                adminUser.setIsActive(true);
                adminUser.setCreatedAt(LocalDateTime.now());

                userRepository.save(adminUser);

                logger.info("Użytkownik administracyjny został pomyślnie utworzony");
            } else {
                logger.debug("Użytkownik administracyjny już istnieje w bazie danych");
            }
        };
    }
}