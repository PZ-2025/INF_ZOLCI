package com.example.backend.performance;

import com.example.backend.dto.LoginRequestDTO;
import com.example.backend.dto.RegisterRequestDTO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Testy wydajności dla AuthController.
 * Testuje endpointy uwierzytelniania i rejestracji pod obciążeniem.
 *
 * @author Jakub
 * @version 1.0.0
 */
@Component
public class AuthControllerPerformanceTest extends BasePerformanceTest {

    private final AtomicInteger userCounter = new AtomicInteger(0);
    private final Random random = new Random();

    /**
     * Wykonuje wszystkie testy wydajności dla AuthController.
     */
    public List<PerformanceResults> runAllTests() {
        List<PerformanceResults> results = new ArrayList<>();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔐 AUTH CONTROLLER PERFORMANCE TESTS");
        System.out.println("=".repeat(80));

        // Test rejestracji użytkowników
        results.add(testUserRegistration());

        // Test logowania użytkowników
        results.add(testUserLogin());

        // Test sprawdzania dostępności nazwy użytkownika
        results.add(testUsernameAvailabilityCheck());

        // Test sprawdzania dostępności emaila
        results.add(testEmailAvailabilityCheck());

        return results;
    }

    /**
     * Test wydajności rejestracji użytkowników.
     */
    private PerformanceResults testUserRegistration() {
        return executeLoadTest(
                "User Registration",
                PerformanceTestConfig.TestType.MEDIUM_LOAD,
                this::performUserRegistration
        );
    }

    /**
     * Test wydajności logowania użytkowników.
     */
    private PerformanceResults testUserLogin() {
        // Najpierw zarejestruj kilku użytkowników do testów logowania
        createTestUsers(20);

        return executeLoadTest(
                "User Login",
                PerformanceTestConfig.TestType.HEAVY_LOAD,
                this::performUserLogin
        );
    }

    /**
     * Test sprawdzania dostępności nazwy użytkownika.
     */
    private PerformanceResults testUsernameAvailabilityCheck() {
        return executeLoadTest(
                "Username Availability Check",
                PerformanceTestConfig.TestType.LIGHT_LOAD,
                this::performUsernameAvailabilityCheck
        );
    }

    /**
     * Test sprawdzania dostępności emaila.
     */
    private PerformanceResults testEmailAvailabilityCheck() {
        return executeLoadTest(
                "Email Availability Check",
                PerformanceTestConfig.TestType.LIGHT_LOAD,
                this::performEmailAvailabilityCheck
        );
    }

    /**
     * Wykonuje rejestrację użytkownika.
     */
    private RequestResult performUserRegistration() {
        long startTime = System.currentTimeMillis();

        try {
            int userId = userCounter.incrementAndGet();
            RegisterRequestDTO request = new RegisterRequestDTO();
            request.setUsername("testuser" + userId + "_" + random.nextInt(10000));
            request.setPassword("password123");
            request.setEmail("testuser" + userId + "_" + random.nextInt(10000) + "@example.com");
            request.setFirstName("Test");
            request.setLastName("User");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<RegisterRequestDTO> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    getBaseUrl() + "/api/auth/register", entity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            boolean success = response.getStatusCode().is2xxSuccessful();

            return new RequestResult(responseTime, success, response.getStatusCode().value(), null);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new RequestResult(responseTime, false, 0, e.getMessage());
        }
    }

    /**
     * Wykonuje logowanie użytkownika.
     */
    private RequestResult performUserLogin() {
        long startTime = System.currentTimeMillis();

        try {
            // Użyj losowego użytkownika testowego
            int userNum = random.nextInt(20) + 1;
            LoginRequestDTO request = new LoginRequestDTO();
            request.setUsername("logintest" + userNum);
            request.setPassword("password123");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<LoginRequestDTO> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    getBaseUrl() + "/api/auth/login", entity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            boolean success = response.getStatusCode().is2xxSuccessful();

            return new RequestResult(responseTime, success, response.getStatusCode().value(), null);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new RequestResult(responseTime, false, 0, e.getMessage());
        }
    }

    /**
     * Sprawdza dostępność nazwy użytkownika.
     */
    private RequestResult performUsernameAvailabilityCheck() {
        long startTime = System.currentTimeMillis();

        try {
            String username = "checkuser" + random.nextInt(100000);
            ResponseEntity<String> response = restTemplate.getForEntity(
                    getBaseUrl() + "/api/auth/check/username/" + username, String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            boolean success = response.getStatusCode().is2xxSuccessful();

            return new RequestResult(responseTime, success, response.getStatusCode().value(), null);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new RequestResult(responseTime, false, 0, e.getMessage());
        }
    }

    /**
     * Sprawdza dostępność emaila.
     */
    private RequestResult performEmailAvailabilityCheck() {
        long startTime = System.currentTimeMillis();

        try {
            String email = "checkemail" + random.nextInt(100000) + "@example.com";
            ResponseEntity<String> response = restTemplate.getForEntity(
                    getBaseUrl() + "/api/auth/check/email/" + email, String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            boolean success = response.getStatusCode().is2xxSuccessful();

            return new RequestResult(responseTime, success, response.getStatusCode().value(), null);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new RequestResult(responseTime, false, 0, e.getMessage());
        }
    }

    /**
     * Tworzy użytkowników testowych do testów logowania.
     */
    private void createTestUsers(int count) {
        for (int i = 1; i <= count; i++) {
            try {
                RegisterRequestDTO request = new RegisterRequestDTO();
                request.setUsername("logintest" + i);
                request.setPassword("password123");
                request.setEmail("logintest" + i + "@example.com");
                request.setFirstName("Login");
                request.setLastName("Test");

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<RegisterRequestDTO> entity = new HttpEntity<>(request, headers);

                restTemplate.postForEntity(getBaseUrl() + "/api/auth/register", entity, String.class);
            } catch (Exception e) {
                // Ignoruj błędy przy tworzeniu użytkowników testowych
            }
        }
    }
}