package com.example.database;

import com.example.database.models.*;
import com.example.database.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class DataInitializerConfig {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PriorityRepository priorityRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;

    @Bean
    @Profile("dev") // Ten bean będzie aktywny tylko w profilu 'dev'
    public CommandLineRunner initDevData() {
        return args -> {
            System.out.println("Sprawdzanie danych testowych...");

            // Pobierz istniejące dane
            Optional<User> adminOpt = userRepository.findByUsername("admin");
            User admin = adminOpt.orElseGet(() -> {
                System.out.println("Użytkownik 'admin' nie znaleziony, pomijam tworzenie");
                return null;
            });

            // Sprawdź, czy istnieją dodatkowi użytkownicy testowi (których nie ma w migracji)
            Optional<User> managerOpt = userRepository.findByUsername("manager1");
            User manager;
            if (managerOpt.isPresent()) {
                manager = managerOpt.get();
                System.out.println("Użytkownik 'manager1' już istnieje, używam istniejącego");
            } else {
                manager = createUser("manager1", "Fifonż", "Wiśniewski", "manager1@example.com", "kierownik");
                System.out.println("Utworzono użytkownika 'manager1'");
            }

            // Dodaj pozostałych testowych użytkowników, jeśli nie istnieją
            User user1 = userRepository.findByUsername("user1")
                    .orElseGet(() -> createUser("user1", "Jan", "Kowalski", "jan@example.com", "pracownik"));

            User user2 = userRepository.findByUsername("user2")
                    .orElseGet(() -> createUser("user2", "Krzysztof", "Nowak", "krzysztof@example.com", "pracownik"));

            // Sprawdź, czy istnieją zespoły testowe
            List<Team> existingTeams = teamRepository.findAll();
            Team team1;
            Team team2;

            if (existingTeams.stream().anyMatch(t -> t.getName().equals("Zespół szybkiego reagowania A1"))) {
                team1 = existingTeams.stream()
                        .filter(t -> t.getName().equals("Zespół szybkiego reagowania A1"))
                        .findFirst()
                        .get();
                System.out.println("Zespół 'Zespół szybkiego reagowania A1' już istnieje, używam istniejącego");
            } else {
                team1 = createTeam("Zespół szybkiego reagowania A1", manager);
                System.out.println("Utworzono zespół 'Zespół szybkiego reagowania A1'");
            }

            if (existingTeams.stream().anyMatch(t -> t.getName().equals("Zespół ekspertów budowlanych E1"))) {
                team2 = existingTeams.stream()
                        .filter(t -> t.getName().equals("Zespół ekspertów budowlanych E1"))
                        .findFirst()
                        .get();
                System.out.println("Zespół 'Zespół ekspertów budowlanych E1' już istnieje, używam istniejącego");
            } else {
                team2 = createTeam("Zespół ekspertów budowlanych E1", manager);
                System.out.println("Utworzono zespół 'Zespół ekspertów budowlanych E1'");
            }

            // Dodawanie członków do zespołów, jeśli jeszcze nie są przypisani
            if (!isUserInTeam(user1, team1)) {
                addMemberToTeam(user1, team1);
                System.out.println("Dodano użytkownika " + user1.getUsername() + " do zespołu " + team1.getName());
            }

            if (!isUserInTeam(user2, team1)) {
                addMemberToTeam(user2, team1);
                System.out.println("Dodano użytkownika " + user2.getUsername() + " do zespołu " + team1.getName());
            }

            if (!isUserInTeam(user1, team2)) {
                addMemberToTeam(user1, team2);
                System.out.println("Dodano użytkownika " + user1.getUsername() + " do zespołu " + team2.getName());
            }

            // Pobierz priorytety i statusy - te powinny już istnieć z migracji Liquibase
            List<Priority> priorities = priorityRepository.findAll();
            List<TaskStatus> statuses = taskStatusRepository.findAll();

            if (priorities.isEmpty()) {
                System.out.println("UWAGA: Brak priorytetów w bazie danych! Zadania nie zostaną utworzone.");
                return;
            }

            if (statuses.isEmpty()) {
                System.out.println("UWAGA: Brak statusów zadań w bazie danych! Zadania nie zostaną utworzone.");
                return;
            }

            System.out.println("Znaleziono " + priorities.size() + " priorytetów i " + statuses.size() + " statusów zadań");

            // Sprawdź, czy zadania już istnieją
            String task1Title = "Remont mieszkania ul. Załęska 76";
            String task2Title = "Budowa werandy dla domu jednorodzinnego";

            if (taskRepository.findByTitle(task1Title).isEmpty()) {
                Task task1 = createTask(task1Title,
                        "Klient ma problemy z wilgocią, najprawdopodobniej po ostatniej powodzi",
                        team1, priorities.get(2), statuses.get(0), manager,
                        LocalDate.now(), LocalDate.now().plusDays(7));
                System.out.println("Utworzono zadanie: " + task1Title);

                // Dodaj komentarze tylko do nowo utworzonych zadań
                addComment(task1, user1, "Zgubiłem klucze");
                addComment(task1, manager, "Sprawdź wszystkie kieszenie");
            } else {
                System.out.println("Zadanie '" + task1Title + "' już istnieje, pomijam");
            }

            if (taskRepository.findByTitle(task2Title).isEmpty()) {
                Task task2 = createTask(task2Title,
                        "Klient poprosił o wybudowę werandy o powierzchni ok. 18m kwadratowych",
                        team1, priorities.get(1), statuses.get(1), user1,
                        LocalDate.now().minusDays(2), LocalDate.now().plusDays(3));
                System.out.println("Utworzono zadanie: " + task2Title);

                addComment(task2, user2, "Najprawdopodobniej dostaliśmy za mały budżet na materiały!");
            } else {
                System.out.println("Zadanie '" + task2Title + "' już istnieje, pomijam");
            }

            System.out.println("Inicjalizacja danych testowych zakończona!");
        };
    }

    // Metoda sprawdzająca, czy użytkownik jest już członkiem zespołu
    private boolean isUserInTeam(User user, Team team) {
        return teamMemberRepository.findByUserAndTeam(user, team).isPresent();
    }

    private User createUser(String username, String firstName, String lastName, String email, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("$2a$10$rJLq2nt5H1ot.ZQfzwQ.e.TY7ABh2stNesvTRWh2UDpaVzM9JL.yC"); // 'password' zahaszowane
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private Team createTeam(String name, User manager) {
        Team team = new Team();
        team.setName(name);
        team.setManager(manager);
        team.setCreatedAt(LocalDateTime.now());
        team.setIsActive(true);
        return teamRepository.save(team);
    }

    private TeamMember addMemberToTeam(User user, Team team) {
        TeamMember member = new TeamMember();
        member.setUser(user);
        member.setTeam(team);
        member.setJoinedAt(LocalDateTime.now());
        member.setIsActive(true);
        return teamMemberRepository.save(member);
    }

    private Task createTask(String title, String description, Team team, Priority priority,
                            TaskStatus status, User createdBy, LocalDate startDate, LocalDate deadline) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setTeam(team);
        task.setPriority(priority);
        task.setStatus(status);
        task.setCreatedBy(createdBy);
        task.setStartDate(startDate);
        task.setDeadline(deadline);
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    private TaskComment addComment(Task task, User user, String comment) {
        TaskComment taskComment = new TaskComment();
        taskComment.setTask(task);
        taskComment.setUser(user);
        taskComment.setComment(comment);
        taskComment.setCreatedAt(LocalDateTime.now());
        return taskCommentRepository.save(taskComment);
    }
}