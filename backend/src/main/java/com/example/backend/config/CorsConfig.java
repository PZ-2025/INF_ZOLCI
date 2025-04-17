package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Konfiguracja CORS (Cross-Origin Resource Sharing) dla aplikacji BuildTask.
 *
 * <p>Ta konfiguracja pozwala na komunikację między frontendem a backendem,
 * gdy są hostowane na różnych domenach, portach lub protokołach.</p>
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
public class CorsConfig {

    /**
     * Konfiguruje filtr CORS, który będzie stosowany do wszystkich żądań HTTP.
     *
     * <p>Ustawienia:</p>
     * <ul>
     *   <li>Zezwala na pochodzenie żądań z frontenda (localhost na różnych portach)</li>
     *   <li>Zezwala na wszystkie metody HTTP (GET, POST, PUT, DELETE, OPTIONS, itd.)</li>
     *   <li>Zezwala na wszystkie nagłówki</li>
     *   <li>Zezwala na przekazywanie poświadczeń (cookies, nagłówki autoryzacji)</li>
     *   <li>Ustawia maksymalny czas ważności pre-flight (OPTIONS) na 3600 sekund</li>
     * </ul>
     *
     * @return Skonfigurowany filtr CORS
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Zezwól na żądania z tych źródeł
        config.addAllowedOrigin("http://localhost:3000"); // Frontend na porcie 3000
        config.addAllowedOrigin("http://localhost:5173"); // Vite dev server
        config.addAllowedOrigin("http://localhost:4200"); // Dla Angular (jeśli będzie używany)
        config.addAllowedOrigin("http://localhost:8081"); // Dla innych frontendów
        config.addAllowedOrigin("electron://localhost"); // Dla aplikacji Electron

        // Zezwól na wszystkie metody HTTP
        config.addAllowedMethod("*");

        // Zezwól na wszystkie nagłówki
        config.addAllowedHeader("*");

        // Zezwól na credentials (cookies, auth headers)
        config.setAllowCredentials(true);

        // Ustaw czas ważności pre-flight
        config.setMaxAge(3600L);

        // Zastosuj konfigurację do wszystkich ścieżek
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}