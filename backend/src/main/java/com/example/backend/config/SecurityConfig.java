package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Konfiguracja bezpieczeństwa systemu.
 * <p>
 * Dostarcza beany związane z bezpieczeństwem, takie jak enkoder haseł.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class SecurityConfig {

    /**
     * Tworzy i udostępnia bean encodera haseł BCrypt.
     *
     * @return instancja BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}