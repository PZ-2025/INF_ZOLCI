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
    public CommandLineRunner initDevData()
    {
        return args ->
        {
            // Sprawdź czy dane już istnieją
            if (userRepository.count() > 0) {
                return; // Nie inicjalizuj, jeśli dane już istnieją
            }

            System.out.println("Inicjalizacja danych testowych...");

            // Tworzenie użytkowników testowych
            User admin = createUser("admin", "Admin", "Admin", "admin@example.com", "admin");
            User manager = createUser("manager1", "Fifonż", "Wiśniewski", "manager1@example.com", "kierownik");
            User user1 = createUser("user1", "Jan", "Kowalski", "jan@example.com", "pracownik");
            User user2 = createUser("user2", "Krzysztof", "Nowak", "krzysztof@example.com", "pracownik");

            // Tworzenie zespołów
            Team team1 = createTeam("Zespół szybkiego reagowania A1", manager);
            Team team2 = createTeam("Zespół ekspertów budowlanych E1", manager);

            // Dodawanie członków do zespołów
            addMemberToTeam(user1, team1);
            addMemberToTeam(user2, team1);
            addMemberToTeam(user1, team2);

            // Pobierz priorytety i statusy
            List<Priority> priorities = priorityRepository.findAll();
            List<TaskStatus> statuses = taskStatusRepository.findAll();

            if (!priorities.isEmpty() && !statuses.isEmpty())
            {
                // Tworzenie zadań
                Task task1 = createTask("Remont mieszkania ul. Załęska 76", "Klient ma problemy z wilogcią, najprawdopodbniej po ostatniej powodzi",
                        team1, priorities.get(2), statuses.get(0), manager, LocalDate.now(), LocalDate.now().plusDays(7));

                Task task2 = createTask("Budowa werandy dla domu jednorodzinnego", "Klient poprosił o wybudowę werandy o powierzchni ok. 18m kwadratowych",
                        team1, priorities.get(1), statuses.get(1), user1, LocalDate.now().minusDays(2), LocalDate.now().plusDays(3));

                // Dodawanie komentarzy
                addComment(task1, user1, "Zgubiłem klucze");
                addComment(task1, manager, "Sprawdź wszystkie kieszenie");
                addComment(task2, user2, "Najprawdopodbniej dostaliśmy za mały budżet na materiały!");
            }

            System.out.println("Dane testowe zostały zainicjalizowane!");
        };
    }

    private User createUser(String username, String firstName, String lastName, String email, String role)
    {
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

    private Team createTeam(String name, User manager)
    {
        Team team = new Team();
        team.setName(name);
        team.setManager(manager);
        team.setCreatedAt(LocalDateTime.now());
        team.setIsActive(true);
        return teamRepository.save(team);
    }

    private TeamMember addMemberToTeam(User user, Team team)
    {
        TeamMember member = new TeamMember();
        member.setUser(user);
        member.setTeam(team);
        member.setJoinedAt(LocalDateTime.now());
        member.setIsActive(true);
        return teamMemberRepository.save(member);
    }

    private Task createTask(String title, String description, Team team, Priority priority,
                            TaskStatus status, User createdBy, LocalDate startDate, LocalDate deadline)
    {
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

    private TaskComment addComment(Task task, User user, String comment)
    {
        TaskComment taskComment = new TaskComment();
        taskComment.setTask(task);
        taskComment.setUser(user);
        taskComment.setComment(comment);
        taskComment.setCreatedAt(LocalDateTime.now());
        return taskCommentRepository.save(taskComment);
    }
}