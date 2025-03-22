package com.example.database;

import com.example.database.models.*;
import com.example.database.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class DataBuildConfig  {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;

    // Lista nazw użytkowników dodanych przez profil "dev"
    private final List<String> devUsernames = Arrays.asList("manager1", "user1", "user2");

    // Lista nazw zespołów dodanych przez profil "dev"
    private final List<String> devTeamNames = Arrays.asList(
            "Zespół szybkiego reagowania A1",
            "Zespół ekspertów budowlanych E1"
    );

    // Lista tytułów zadań dodanych przez profil "dev"
    private final List<String> devTaskTitles = Arrays.asList(
            "Remont mieszkania ul. Załęska 76",
            "Budowa werandy dla domu jednorodzinnego"
    );

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
                Optional<Object> teamOptional = teamRepository.findByName(teamName);
                if (teamOptional.isPresent() && teamOptional.get() instanceof Team) {
                    Team team = (Team) teamOptional.get();
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