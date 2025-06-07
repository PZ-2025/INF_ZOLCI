package com.example.backend.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstrakcyjna klasa bazowa dla testów wydajności API.
 * Zapewnia infrastrukturę do testowania obciążeniowego endpointów REST.
 *
 * @author Performance Team
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("performance")
public abstract class BasePerformanceTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * Wyniki pojedynczego żądania HTTP.
     */
    protected static class RequestResult {
        private final long responseTimeMs;
        private final boolean success;
        private final String errorMessage;
        private final int statusCode;

        public RequestResult(long responseTimeMs, boolean success, int statusCode, String errorMessage) {
            this.responseTimeMs = responseTimeMs;
            this.success = success;
            this.statusCode = statusCode;
            this.errorMessage = errorMessage;
        }

        public long getResponseTimeMs() { return responseTimeMs; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public int getStatusCode() { return statusCode; }
    }

    /**
     * Agregowane wyniki testu wydajności.
     */
    protected static class PerformanceResults {
        private final String testName;
        private final List<RequestResult> results;
        private final long totalDurationMs;
        private final int totalRequests;
        private final int successfulRequests;
        private final double successRate;
        private final double throughputRps;
        private final double avgResponseTimeMs;
        private final long minResponseTimeMs;
        private final long maxResponseTimeMs;
        private final long p95ResponseTimeMs;
        private final long p99ResponseTimeMs;

        public PerformanceResults(String testName, List<RequestResult> results, long totalDurationMs) {
            this.testName = testName;
            this.results = new ArrayList<>(results);
            this.totalDurationMs = totalDurationMs;
            this.totalRequests = results.size();
            this.successfulRequests = (int) results.stream().filter(RequestResult::isSuccess).count();
            this.successRate = totalRequests > 0 ? (double) successfulRequests / totalRequests : 0.0;
            this.throughputRps = totalDurationMs > 0 ? (double) totalRequests / (totalDurationMs / 1000.0) : 0.0;

            List<Long> responseTimes = results.stream()
                    .mapToLong(RequestResult::getResponseTimeMs)
                    .sorted()
                    .boxed()
                    .toList();

            this.avgResponseTimeMs = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
            this.minResponseTimeMs = responseTimes.isEmpty() ? 0 : responseTimes.get(0);
            this.maxResponseTimeMs = responseTimes.isEmpty() ? 0 : responseTimes.get(responseTimes.size() - 1);
            this.p95ResponseTimeMs = getPercentile(responseTimes, 0.95);
            this.p99ResponseTimeMs = getPercentile(responseTimes, 0.99);
        }

        private long getPercentile(List<Long> sortedValues, double percentile) {
            if (sortedValues.isEmpty()) return 0;
            int index = (int) Math.ceil(sortedValues.size() * percentile) - 1;
            return sortedValues.get(Math.max(0, Math.min(index, sortedValues.size() - 1)));
        }

        // Getters
        public String getTestName() { return testName; }
        public int getTotalRequests() { return totalRequests; }
        public int getSuccessfulRequests() { return successfulRequests; }
        public double getSuccessRate() { return successRate; }
        public double getThroughputRps() { return throughputRps; }
        public double getAvgResponseTimeMs() { return avgResponseTimeMs; }
        public long getMinResponseTimeMs() { return minResponseTimeMs; }
        public long getMaxResponseTimeMs() { return maxResponseTimeMs; }
        public long getP95ResponseTimeMs() { return p95ResponseTimeMs; }
        public long getP99ResponseTimeMs() { return p99ResponseTimeMs; }
        public long getTotalDurationMs() { return totalDurationMs; }
        public List<RequestResult> getResults() { return results; }
    }

    /**
     * Wykonuje test obciążeniowy dla podanej funkcji.
     */
    protected PerformanceResults executeLoadTest(String testName,
                                                 PerformanceTestConfig.TestType testType,
                                                 Callable<RequestResult> testFunction) {

        int numUsers = testType.getUsers();
        int requestsPerUser = testType.getRequests();
        int rampUpSeconds = testType.getRampUp();

        System.out.printf("🚀 Starting %s - Users: %d, Requests per user: %d, Ramp-up: %ds%n",
                testName, numUsers, requestsPerUser, rampUpSeconds);

        ExecutorService executor = Executors.newFixedThreadPool(numUsers);
        List<Future<List<RequestResult>>> futures = new ArrayList<>();
        AtomicInteger completedUsers = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        try {
            // Uruchom użytkowników z opóźnieniem (ramp-up)
            for (int i = 0; i < numUsers; i++) {
                final int userIndex = i;
                Future<List<RequestResult>> future = executor.submit(() -> {
                    List<RequestResult> userResults = new ArrayList<>();

                    // Wykonaj żądania dla tego użytkownika
                    for (int j = 0; j < requestsPerUser; j++) {
                        try {
                            RequestResult result = testFunction.call();
                            userResults.add(result);

                            // Krótka przerwa między żądaniami
                            Thread.sleep(50);
                        } catch (Exception e) {
                            userResults.add(new RequestResult(0, false, 0, e.getMessage()));
                        }
                    }

                    int completed = completedUsers.incrementAndGet();
                    if (completed % 5 == 0 || completed == numUsers) {
                        System.out.printf("  ⏳ Progress: %d/%d users completed%n", completed, numUsers);
                    }

                    return userResults;
                });
                futures.add(future);

                // Ramp-up delay
                if (rampUpSeconds > 0 && i < numUsers - 1) {
                    Thread.sleep((rampUpSeconds * 1000L) / numUsers);
                }
            }

            // Zbierz wyniki
            List<RequestResult> allResults = new ArrayList<>();
            for (Future<List<RequestResult>> future : futures) {
                try {
                    allResults.addAll(future.get(30, TimeUnit.SECONDS));
                } catch (TimeoutException e) {
                    System.err.println("  ⚠️ Some requests timed out");
                    allResults.add(new RequestResult(30000, false, 0, "Timeout"));
                } catch (ExecutionException e) {
                    System.err.println("  ❌ Execution error: " + e.getCause().getMessage());
                    allResults.add(new RequestResult(0, false, 0, "Execution error: " + e.getCause().getMessage()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("  ⚠️ Test interrupted");
                    allResults.add(new RequestResult(0, false, 0, "Test interrupted"));
                }
            }

            long endTime = System.currentTimeMillis();
            return new PerformanceResults(testName, allResults, endTime - startTime);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Test interrupted", e);
        } finally {
            executor.shutdown();
        }
    }
}