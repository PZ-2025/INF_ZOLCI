package com.example.backend.config;

import com.example.backend.models.TaskComment;
import com.example.backend.models.Team;
import com.example.backend.models.TeamMember;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Konfiguracja czyszczenia danych testowych w aplikacji BuildTask.
 *
 * <p>Klasa odpowiedzialna za usuwanie danych wygenerowanych w środowisku deweloperskim.
 * Przeznaczona do użycia w profilu "build", umożliwia systematyczne usuwanie
 * tymczasowych danych testowych z bazy danych.</p>
 *
 * <p>Proces czyszczenia obejmuje:</p>
 * <ul>
 *   <li>Usuwanie komentarzy do zadań</li>
 *   <li>Usuwanie zadań testowych</li>
 *   <li>Usuwanie członków zespołów</li>
 *   <li>Usuwanie zespołów testowych</li>
 *   <li>Usuwanie użytkowników testowych</li>
 * </ul>
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class DataBuildConfig  {

    /** Repozytorium użytkowników do zarządzania danymi użytkowników. */
    private final UserRepository userRepository;

    /** Repozytorium zespołów do zarządzania danymi zespołów. */
    private final TeamRepository teamRepository;

    /** Repozytorium członków zespołów do zarządzania przypisaniami użytkowników. */
    private final TeamMemberRepository teamMemberRepository;

    /** Repozytorium zadań do zarządzania danymi zadań. */
    private final TaskRepository taskRepository;

    /** Repozytorium komentarzy do zadań do zarządzania komentarzami. */
    private final TaskCommentRepository taskCommentRepository;

    /** Lista nazw użytkowników wygenerowanych w środowisku deweloperskim. */
    private final List<String> devUsernames = Arrays.asList("manager1", "user1", "user2");

    /** Lista nazw zespołów utworzonych w środowisku deweloperskim. */
    private final List<String> devTeamNames = Arrays.asList(
            "Zespół szybkiego reagowania A1",
            "Zespół ekspertów budowlanych E1"
    );

    /** Lista tytułów zadań wygenerowanych w środowisku deweloperskim. */
    private final List<String> devTaskTitles = Arrays.asList(
            "Remont mieszkania ul. Załęska 76",
            "Budowa werandy dla domu jednorodzinnego"
    );

    /**
     * Tworzy {@link CommandLineRunner} do czyszczenia danych testowych.
     *
     * <p>Metoda realizuje sekwencyjne usuwanie danych testowych:</p>
     * <ol>
     *   <li>Usuwa komentarze do zadań</li>
     *   <li>Usuwa zadania testowe</li>
     *   <li>Usuwa członków zespołów</li>
     *   <li>Usuwa zespoły testowe</li>
     *   <li>Usuwa użytkowników testowych</li>
     * </ol>
     *
     * <p>Aktywna tylko w profilu 'build'.</p>
     *
     * @return {@link CommandLineRunner} do czyszczenia danych
     */
    @Bean
    @Profile("build") // Ten bean będzie aktywny tylko w profilu 'build'
    public CommandLineRunner cleanDevData() {
        return args -> {
            System.out.println("Rozpoczynam czyszczenie danych testowych...");

            // Najpierw usuwamy komentarze do zadań testowych
            for (String taskTitle : devTaskTitles) {
                taskRepository.findByTitle(taskTitle).ifPresent(task -> {
                    List<TaskComment> comments = taskCommentRepository.findByTask(task);
                    if (!comments.isEmpty()) {
                        System.out.println("Usuwam " + comments.size() + " komentarzy dla zadania: " + taskTitle);
                        taskCommentRepository.deleteAll(comments);
                    }
                });
            }

            // Usuwamy zadania testowe
            for (String taskTitle : devTaskTitles) {
                taskRepository.findByTitle(taskTitle).ifPresent(task -> {
                    System.out.println("Usuwam zadanie: " + taskTitle);
                    taskRepository.delete(task);
                });
            }

            // Usuwamy członkostwa w zespołach testowych
            for (String teamName : devTeamNames) {
                Optional<Team> teamOptional = teamRepository.findByName(teamName);
                if (teamOptional.isPresent()) {
                    Team team = teamOptional.get();
                    List<TeamMember> members = teamMemberRepository.findAllByTeam(team);
                    if (!members.isEmpty()) {
                        System.out.println("Usuwam " + members.size() + " członków zespołu: " + teamName);
                        teamMemberRepository.deleteAll(members);
                    }

                    System.out.println("Usuwam zespół: " + teamName);
                    teamRepository.delete(team);
                }
            }

            // Usuwamy użytkowników testowych
            for (String username : devUsernames) {
                userRepository.findByUsername(username).ifPresent(user -> {
                    System.out.println("Usuwam użytkownika: " + username);
                    userRepository.delete(user);
                });
            }

            System.out.println("Czyszczenie danych testowych zakończone!");
        };
    }
}