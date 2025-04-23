package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Konfiguracja obsługi wyjątków i aspektów w aplikacji.
 * <p>
 * Włącza obsługę aspektów dla przechwytywania wyjątków bazodanowych.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableAspectJAutoProxy
public class ExceptionHandlingConfig {
    // Konfiguracja może być rozszerzona o dodatkowe właściwości i beany
}