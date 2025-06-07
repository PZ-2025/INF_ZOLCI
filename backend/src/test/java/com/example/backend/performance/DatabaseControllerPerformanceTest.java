package com.example.backend.performance;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Testy wydajności dla kontrolerów bazy danych.
 * Testuje operacje CRUD na różnych encjach pod obciążeniem.
 *
 * @author Performance Team
 * @version 1.0.0
 */
@Component
public class DatabaseControllerPerformanceTest extends BasePerformanceTest {

    private final Random random = new Random();
    private final List<Integer> createdUserIds = new ArrayList<>();
    private final List<Integer> createdTeamIds = new ArrayList<>();
    private final List<Integer> createdTaskIds = new ArrayList<>();

    /**
     * Wykonuje wszystkie testy wydajności dla kontrolerów bazy danych.
     */
    public List<PerformanceResults> runAllTests() {
        List<PerformanceResults> results = new ArrayList<>();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("🗄️ DATABASE CONTROLLERS PERFORMANCE TESTS");
        System.out.println("=".repeat(80));

        // Przygotuj dane testowe
        prepareTestData();

        // Testy kontrolera użytkowników
        results.add(testGetAllUsers());
        results.add(testGetUserById());
        results.add(testCreateUser());

        // Testy kontrolera zespołów
        results.add(testGetAllTeams());
        results.add(testCreateTeam());
        results.add(testGetTeamById());

        // Testy kontrolera zadań
        results.add(testGetAllTasks());
        results.add(testCreateTask());
        results.add(testGetTaskById());

        // Testy kontrolera statusów zadań
        results.add(testGetAllTaskStatuses());

        // Testy kontrolera priorytetów
        results.add(testGetAllPriorities());

        // Test kontrolera zdrowia
        results.add(testHealthCheck());

        return results;
    }

    /**
     * Przygotowuje podstawowe dane testowe.
     */
    private void prepareTestData() {
        System.out.println("📋 Preparing test data...");

        // Utwórz kilku użytkowników testowych
        for (int i = 1; i <= 10; i++) {
            Integer userId = createTestUser("perfuser" + i);
            if (userId != null) {
                createdUserIds.add(userId);
            }
        }

        // Utwórz kilka zespołów testowych
        for (int i = 1; i <= 5; i++) {
            Integer teamId = createTestTeam("Performance Team " + i);
            if (teamId != null) {
                createdTeamIds.add(teamId);
            }
        }

        System.out.println("✅ Test data prepared");
    }

    /**
     * Test pobierania wszystkich użytkowników.
     */
    private PerformanceResults testGetAllUsers() {
        return executeLoadTest(
                "Get All Users",
                PerformanceTestConfig.TestType.HEAVY_LOAD,
                () -> performGetRequest("/database/users")
        );
    }

    /**
     * Test pobierania użytkownika po ID.
     */
    private PerformanceResults testGetUserById() {
        return executeLoadTest(
                "Get User By ID",
                PerformanceTestConfig.TestType.HEAVY_LOAD,
                () -> {
                    if (createdUserIds.isEmpty()) return new RequestResult(0, false, 404, "No test users");
                    int userId = createdUserIds.get(random.nextInt(createdUserIds.size()));
                    return performGetRequest("/database/users/" + userId);
                }
        );
    }

    /**
     * Test tworzenia użytkownika.
     */
    private PerformanceResults testCreateUser() {
        return executeLoadTest(
                "Create User",
                PerformanceTestConfig.TestType.MEDIUM_LOAD,
                this::performCreateUser
        );
    }

    /**
     * Test pobierania wszystkich zespołów.
     */
    private PerformanceResults testGetAllTeams() {
        return executeLoadTest(
                "Get All Teams",
                PerformanceTestConfig.TestType.MEDIUM_LOAD,
                () -> performGetRequest("/database/teams")
        );
    }

    /**
     * Test tworzenia zespołu.
     */
    private PerformanceResults testCreateTeam() {
        return executeLoadTest(
                "Create Team",
                PerformanceTestConfig.TestType.MEDIUM_LOAD,
                this::performCreateTeam
        );
    }

    /**
     * Test pobierania zespołu po ID.
     */
    private PerformanceResults testGetTeamById() {
        return executeLoadTest(
                "Get Team By ID",
                PerformanceTestConfig.TestType.MEDIUM_LOAD,
                () -> {
                    if (createdTeamIds.isEmpty()) return new RequestResult(0, false, 404, "No test teams");
                    int teamId = createdTeamIds.get(random.nextInt(createdTeamIds.size()));
                    return performGetRequest("/database/teams/" + teamId);
                }
        );
    }

    /**
     * Test pobierania wszystkich zadań.
     */
    private PerformanceResults testGetAllTasks() {
        return executeLoadTest(
                "Get All Tasks",
                PerformanceTestConfig.TestType.HEAVY_LOAD,
                () -> performGetRequest("/database/tasks")
        );
    }

    /**
     * Test tworzenia zadania.
     */
    private PerformanceResults testCreateTask() {
        return executeLoadTest(
                "Create Task",
                PerformanceTestConfig.TestType.MEDIUM_LOAD,
                this::performCreateTask
        );
    }

    /**
     * Test pobierania zadania po ID.
     */
    private PerformanceResults testGetTaskById() {
        return executeLoadTest(
                "Get Task By ID",
                PerformanceTestConfig.TestType.MEDIUM_LOAD,
                () -> {
                    if (createdTaskIds.isEmpty()) return new RequestResult(0, false, 404, "No test tasks");
                    int taskId = createdTaskIds.get(random.nextInt(createdTaskIds.size()));
                    return performGetRequest("/database/tasks/" + taskId);
                }
        );
    }

    /**
     * Test pobierania wszystkich statusów zadań.
     */
    private PerformanceResults testGetAllTaskStatuses() {
        return executeLoadTest(
                "Get All Task Statuses",
                PerformanceTestConfig.TestType.LIGHT_LOAD,
                () -> performGetRequest("/database/task-statuses")
        );
    }

    /**
     * Test pobierania wszystkich priorytetów.
     */
    private PerformanceResults testGetAllPriorities() {
        return executeLoadTest(
                "Get All Priorities",
                PerformanceTestConfig.TestType.LIGHT_LOAD,
                () -> performGetRequest("/database/priorities")
        );
    }

    /**
     * Test sprawdzania zdrowia aplikacji.
     */
    private PerformanceResults testHealthCheck() {
        return executeLoadTest(
                "Health Check",
                PerformanceTestConfig.TestType.LIGHT_LOAD,
                () -> performGetRequest("/api/health")
        );
    }

    /**
     * Wykonuje żądanie GET.
     */
    private RequestResult performGetRequest(String endpoint) {
        long startTime = System.currentTimeMillis();

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    getBaseUrl() + endpoint, String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            boolean success = response.getStatusCode().is2xxSuccessful();

            return new RequestResult(responseTime, success, response.getStatusCode().value(), null);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new RequestResult(responseTime, false, 0, e.getMessage());
        }
    }

    /**
     * Wykonuje tworzenie użytkownika.
     */
    private RequestResult performCreateUser() {
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> user = new HashMap<>();
            user.put("username", "perfuser" + random.nextInt(100000));
            user.put("password", "password123");
            user.put("email", "perfuser" + random.nextInt(100000) + "@example.com");
            user.put("firstName", "Performance");
            user.put("lastName", "User");
            user.put("role", "użytkownik");
            user.put("isActive", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(user, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    getBaseUrl() + "/database/users", entity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            boolean success = response.getStatusCode().is2xxSuccessful();

            return new RequestResult(responseTime, success, response.getStatusCode().value(), null);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new RequestResult(responseTime, false, 0, e.getMessage());
        }
    }

    /**
     * Wykonuje tworzenie zespołu.
     */
    private RequestResult performCreateTeam() {
        long startTime = System.currentTimeMillis();

        try {
            if (createdUserIds.isEmpty()) {
                return new RequestResult(0, false, 400, "No manager available");
            }

            Map<String, Object> team = new HashMap<>();
            team.put("name", "Performance Team " + random.nextInt(100000));
            team.put("managerId", createdUserIds.get(random.nextInt(createdUserIds.size())));
            team.put("isActive", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(team, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    getBaseUrl() + "/database/teams", entity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            boolean success = response.getStatusCode().is2xxSuccessful();

            return new RequestResult(responseTime, success, response.getStatusCode().value(), null);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new RequestResult(responseTime, false, 0, e.getMessage());
        }
    }

    /**
     * Wykonuje tworzenie zadania.
     */
    private RequestResult performCreateTask() {
        long startTime = System.currentTimeMillis();

        try {
            if (createdUserIds.isEmpty()) {
                return new RequestResult(0, false, 400, "No user available");
            }

            Map<String, Object> task = new HashMap<>();
            task.put("title", "Performance Task " + random.nextInt(100000));
            task.put("description", "Performance test task");
            task.put("priorityId", 1);
            task.put("statusId", 1);
            task.put("createdById", createdUserIds.get(random.nextInt(createdUserIds.size())));

            if (!createdTeamIds.isEmpty()) {
                task.put("teamId", createdTeamIds.get(random.nextInt(createdTeamIds.size())));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(task, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    getBaseUrl() + "/database/tasks", entity, String.class);

            long responseTime = System.currentTimeMillis() - startTime;
            boolean success = response.getStatusCode().is2xxSuccessful();

            return new RequestResult(responseTime, success, response.getStatusCode().value(), null);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return new RequestResult(responseTime, false, 0, e.getMessage());
        }
    }

    /**
     * Pomocnicza metoda do tworzenia użytkownika testowego.
     */
    private Integer createTestUser(String username) {
        try {
            Map<String, Object> user = new HashMap<>();
            user.put("username", username);
            user.put("password", "password123");
            user.put("email", username + "@example.com");
            user.put("firstName", "Test");
            user.put("lastName", "User");
            user.put("role", "użytkownik");
            user.put("isActive", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(user, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    getBaseUrl() + "/database/users", entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Integer) response.getBody().get("id");
            }
        } catch (Exception e) {
            // Ignoruj błędy
        }
        return null;
    }

    /**
     * Pomocnicza metoda do tworzenia zespołu testowego.
     */
    private Integer createTestTeam(String teamName) {
        try {
            if (createdUserIds.isEmpty()) return null;

            Map<String, Object> team = new HashMap<>();
            team.put("name", teamName);
            team.put("managerId", createdUserIds.get(0));
            team.put("isActive", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(team, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    getBaseUrl() + "/database/teams", entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Integer) response.getBody().get("id");
            }
        } catch (Exception e) {
            // Ignoruj błędy
        }
        return null;
    }
}