package com.example.backend.performance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Główna klasa uruchamiająca wszystkie testy wydajności API.
 * Wykonuje testy obciążeniowe dla wszystkich kontrolerów i generuje raport.
 *
 * @author Jakub
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example.backend")
@ActiveProfiles("performance")
public class PerformanceTestRunner implements CommandLineRunner {

    @Autowired
    private AuthControllerPerformanceTest authControllerTest;

    @Autowired
    private DatabaseControllerPerformanceTest databaseControllerTest;

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "performance");
        System.setProperty("spring.main.web-application-type", "servlet");
        SpringApplication.run(PerformanceTestRunner.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            System.out.println("\n" + "█".repeat(100));
            System.out.println("🚀 BUILDTASK API PERFORMANCE TEST SUITE");
            System.out.println("📅 Started: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            System.out.println("🌐 Profile: performance");
            System.out.println("█".repeat(100));

            long overallStartTime = System.currentTimeMillis();
            List<BasePerformanceTest.PerformanceResults> allResults = new ArrayList<>();

            // Uruchom wszystkie testy
            allResults.addAll(authControllerTest.runAllTests());
            allResults.addAll(databaseControllerTest.runAllTests());

            long overallDuration = System.currentTimeMillis() - overallStartTime;

            // Wygeneruj raport
            generatePerformanceReport(allResults, overallDuration);

            System.out.println("\n🏁 Performance testing completed successfully!");
            System.exit(0);

        } catch (Exception e) {
            System.err.println("\n❌ Performance testing failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Generuje szczegółowy raport wydajności.
     */
    private void generatePerformanceReport(List<BasePerformanceTest.PerformanceResults> results, long overallDuration) {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("📊 PERFORMANCE TEST RESULTS SUMMARY");
        System.out.println("=".repeat(100));

        // Statystyki ogólne
        int totalTests = results.size();
        int totalRequests = results.stream().mapToInt(BasePerformanceTest.PerformanceResults::getTotalRequests).sum();
        int totalSuccessful = results.stream().mapToInt(BasePerformanceTest.PerformanceResults::getSuccessfulRequests).sum();
        double overallSuccessRate = totalRequests > 0 ? (double) totalSuccessful / totalRequests * 100 : 0;
        double overallThroughput = totalRequests / (overallDuration / 1000.0);

        System.out.printf("📈 Overall Statistics:%n");
        System.out.printf("   • Total Tests: %d%n", totalTests);
        System.out.printf("   • Total Requests: %d%n", totalRequests);
        System.out.printf("   • Successful Requests: %d%n", totalSuccessful);
        System.out.printf("   • Overall Success Rate: %.2f%%%n", overallSuccessRate);
        System.out.printf("   • Overall Throughput: %.2f requests/sec%n", overallThroughput);
        System.out.printf("   • Total Duration: %.2f seconds%n", overallDuration / 1000.0);

        System.out.println("\n" + "-".repeat(100));
        System.out.println("📋 DETAILED RESULTS BY TEST");
        System.out.println("-".repeat(100));

        // Nagłówek tabeli
        System.out.printf("%-35s | %8s | %8s | %7s | %8s | %6s | %6s | %6s%n",
                "Test Name", "Requests", "Success", "Rate%", "Tput/s", "Avg", "P95", "P99");
        System.out.println("-".repeat(100));

        // Szczegóły każdego testu
        for (BasePerformanceTest.PerformanceResults result : results) {
            String status = getTestStatus(result);
            System.out.printf("%-35s | %8d | %8d | %6.1f%% | %7.1f | %5.0f | %5d | %5d %s%n",
                    truncateString(result.getTestName(), 35),
                    result.getTotalRequests(),
                    result.getSuccessfulRequests(),
                    result.getSuccessRate() * 100,
                    result.getThroughputRps(),
                    result.getAvgResponseTimeMs(),
                    result.getP95ResponseTimeMs(),
                    result.getP99ResponseTimeMs(),
                    status);
        }

        System.out.println("-".repeat(100));

        // Analiza wydajności
        generatePerformanceAnalysis(results);

        // Ostrzeżenia i rekomendacje
        generateWarningsAndRecommendations(results, overallSuccessRate, overallThroughput);
    }

    /**
     * Generuje analizę wydajności.
     */
    private void generatePerformanceAnalysis(List<BasePerformanceTest.PerformanceResults> results) {
        System.out.println("\n🔍 PERFORMANCE ANALYSIS");
        System.out.println("-".repeat(50));

        // Najszybsze testy
        BasePerformanceTest.PerformanceResults fastest = results.stream()
                .min((a, b) -> Double.compare(a.getAvgResponseTimeMs(), b.getAvgResponseTimeMs()))
                .orElse(null);

        if (fastest != null) {
            System.out.printf("🏃 Fastest Test: %s (%.0f ms avg)%n",
                    fastest.getTestName(), fastest.getAvgResponseTimeMs());
        }

        // Najwolniejsze testy
        BasePerformanceTest.PerformanceResults slowest = results.stream()
                .max((a, b) -> Double.compare(a.getAvgResponseTimeMs(), b.getAvgResponseTimeMs()))
                .orElse(null);

        if (slowest != null) {
            System.out.printf("🐌 Slowest Test: %s (%.0f ms avg)%n",
                    slowest.getTestName(), slowest.getAvgResponseTimeMs());
        }

        // Najwyższa przepustowość
        BasePerformanceTest.PerformanceResults highestThroughput = results.stream()
                .max((a, b) -> Double.compare(a.getThroughputRps(), b.getThroughputRps()))
                .orElse(null);

        if (highestThroughput != null) {
            System.out.printf("⚡ Highest Throughput: %s (%.1f req/s)%n",
                    highestThroughput.getTestName(), highestThroughput.getThroughputRps());
        }

        // Statystyki P95
        double avgP95 = results.stream()
                .mapToLong(BasePerformanceTest.PerformanceResults::getP95ResponseTimeMs)
                .average()
                .orElse(0.0);

        System.out.printf("📊 Average P95 Response Time: %.0f ms%n", avgP95);
    }

    /**
     * Generuje ostrzeżenia i rekomendacje.
     */
    private void generateWarningsAndRecommendations(List<BasePerformanceTest.PerformanceResults> results,
                                                    double overallSuccessRate, double overallThroughput) {
        System.out.println("\n⚠️ WARNINGS & RECOMMENDATIONS");
        System.out.println("-".repeat(50));

        boolean hasWarnings = false;

        // Sprawdź ogólną wydajność
        if (overallSuccessRate < 95.0) {
            System.out.printf("⚠️ Low overall success rate: %.1f%% (expected: >95%%)%n", overallSuccessRate);
            hasWarnings = true;
        }

        if (overallThroughput < PerformanceTestConfig.PerformanceThresholds.MIN_THROUGHPUT_RPS) {
            System.out.printf("⚠️ Low overall throughput: %.1f req/s (expected: >%.1f req/s)%n",
                    overallThroughput, PerformanceTestConfig.PerformanceThresholds.MIN_THROUGHPUT_RPS);
            hasWarnings = true;
        }

        // Sprawdź poszczególne testy
        for (BasePerformanceTest.PerformanceResults result : results) {
            if (result.getSuccessRate() < 0.99) {
                System.out.printf("⚠️ %s: Low success rate %.1f%%%n",
                        result.getTestName(), result.getSuccessRate() * 100);
                hasWarnings = true;
            }

            if (result.getP95ResponseTimeMs() > PerformanceTestConfig.PerformanceThresholds.MAX_95TH_PERCENTILE_MS) {
                System.out.printf("⚠️ %s: High P95 response time %d ms (expected: <%d ms)%n",
                        result.getTestName(), result.getP95ResponseTimeMs(),
                        PerformanceTestConfig.PerformanceThresholds.MAX_95TH_PERCENTILE_MS);
                hasWarnings = true;
            }
        }

        if (!hasWarnings) {
            System.out.println("✅ All performance metrics are within acceptable ranges!");
        }

        // Rekomendacje
        System.out.println("\n💡 RECOMMENDATIONS");
        System.out.println("-".repeat(50));
        System.out.println("• Monitor database connection pool settings");
        System.out.println("• Consider adding database indexes for frequently queried fields");
        System.out.println("• Implement caching for frequently accessed data");
        System.out.println("• Add API rate limiting to prevent abuse");
        System.out.println("• Monitor memory usage during peak loads");
    }

    /**
     * Określa status testu na podstawie wyników.
     */
    private String getTestStatus(BasePerformanceTest.PerformanceResults result) {
        if (result.getSuccessRate() < 0.95) {
            return "🔴";
        } else if (result.getSuccessRate() < 0.99 ||
                result.getP95ResponseTimeMs() > PerformanceTestConfig.PerformanceThresholds.MAX_95TH_PERCENTILE_MS) {
            return "🟡";
        } else {
            return "🟢";
        }
    }

    /**
     * Obcina tekst do określonej długości.
     */
    private String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}