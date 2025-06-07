package com.example.backend.performance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Konfiguracja testów wydajności API.
 * Definiuje parametry testowania obciążeniowego dla różnych endpointów.
 *
 * @author Jakub
 * @version 1.0.0
 */
@Configuration
@Profile("performance")
public class PerformanceTestConfig {

    @Value("${performance.test.concurrent-users:10}")
    private int concurrentUsers;

    @Value("${performance.test.requests-per-user:100}")
    private int requestsPerUser;

    @Value("${performance.test.ramp-up-seconds:5}")
    private int rampUpSeconds;

    @Value("${performance.test.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${performance.test.think-time-ms:100}")
    private long thinkTimeMs;

    /**
     * Parametry testów wydajności dla różnych typów operacji.
     */
    public enum TestType {
        LIGHT_LOAD(5, 50, 2),      // Lekkie obciążenie
        MEDIUM_LOAD(10, 100, 5),   // Średnie obciążenie
        HEAVY_LOAD(20, 200, 10),   // Ciężkie obciążenie
        STRESS_TEST(50, 500, 15);  // Test przeciążenia

        private final int users;
        private final int requests;
        private final int rampUp;

        TestType(int users, int requests, int rampUp) {
            this.users = users;
            this.requests = requests;
            this.rampUp = rampUp;
        }

        public int getUsers() { return users; }
        public int getRequests() { return requests; }
        public int getRampUp() { return rampUp; }
    }

    /**
     * Konfiguracja dla różnych kontrolerów.
     */
    public enum ControllerTestConfig {
        AUTH_CONTROLLER("Auth Controller", TestType.MEDIUM_LOAD),
        HEALTH_CONTROLLER("Health Controller", TestType.LIGHT_LOAD),
        TASK_CONTROLLER("Task Controller", TestType.HEAVY_LOAD),
        USER_CONTROLLER("User Controller", TestType.MEDIUM_LOAD),
        TEAM_CONTROLLER("Team Controller", TestType.MEDIUM_LOAD),
        PRIORITY_CONTROLLER("Priority Controller", TestType.LIGHT_LOAD),
        REPORT_CONTROLLER("Report Controller", TestType.MEDIUM_LOAD);

        private final String displayName;
        private final TestType testType;

        ControllerTestConfig(String displayName, TestType testType) {
            this.displayName = displayName;
            this.testType = testType;
        }

        public String getDisplayName() { return displayName; }
        public TestType getTestType() { return testType; }
    }

    // Getters
    public int getConcurrentUsers() { return concurrentUsers; }
    public int getRequestsPerUser() { return requestsPerUser; }
    public int getRampUpSeconds() { return rampUpSeconds; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public long getThinkTimeMs() { return thinkTimeMs; }

    /**
     * Maksymalne dopuszczalne czasy odpowiedzi (w ms) dla różnych operacji.
     */
    public static class ResponseTimeThresholds {
        public static final long READ_OPERATION_MS = 200;
        public static final long WRITE_OPERATION_MS = 500;
        public static final long COMPLEX_OPERATION_MS = 1000;
        public static final long REPORT_GENERATION_MS = 3000;
    }

    /**
     * Progi dla wskaźników wydajności.
     */
    public static class PerformanceThresholds {
        public static final double MAX_ERROR_RATE = 0.01; // 1%
        public static final double MIN_THROUGHPUT_RPS = 10.0; // Requests per second
        public static final int MAX_95TH_PERCENTILE_MS = 1000;
    }
}