package com.example.backend.config;

import com.example.backend.models.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;

/**
 * Konfiguracja czyszczenia danych w aplikacji BuildTask.
 *
 * <p>Klasa odpowiedzialna za usuwanie danych wprowadzonych przez użytkowników,
 * zachowując tylko podstawowe dane zdefiniowane w db/changelog/04-insert-basic-data.xml.</p>
 *
 * @author Jakub
 * @version 2.0.0
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class DataBuildConfig {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskRepository taskRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final ReportRepository reportRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final PriorityRepository priorityRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final SystemSettingRepository systemSettingRepository;

    // Dane podstawowe z 04-insert-basic-data.xml
    private final String ADMIN_USERNAME = "admin";
    private final List<String> BASIC_PRIORITIES = Arrays.asList("Niski", "Średni", "Wysoki");
    private final List<String> BASIC_STATUSES = Arrays.asList("Rozpoczęte", "W toku", "Zakończone");
    private final List<String> BASIC_REPORT_TYPES = Arrays.asList(
            "Raport postępu budowy",
            "Raport obciążenia pracownika",
            "Raport efektywności zespołu"
    );
    private final String BASIC_SYSTEM_SETTING_KEY = "app.name";

    /**
     * Tworzy {@link CommandLineRunner} do czyszczenia wszystkich danych oprócz podstawowych.
     *
     * <p>Metoda realizuje sekwencyjne usuwanie danych z zachowaniem danych podstawowych:</p>
     *
     * <p>Aktywna tylko w profilu 'production'.</p>
     *
     * @return {@link CommandLineRunner} do czyszczenia danych
     */
    @Bean
    @Profile("production")
    public CommandLineRunner cleanAllExceptBasicData() {
        return args -> {
            System.out.println("Rozpoczynam proces czyszczenia danych - zachowując tylko dane podstawowe...");

            // 1. Usuwanie wszystkich komentarzy do zadań
            System.out.println("Usuwanie wszystkich komentarzy do zadań...");
            taskCommentRepository.deleteAll();

            // 2. Usuwanie całej historii zadań
            System.out.println("Usuwanie całej historii zadań...");
            taskHistoryRepository.deleteAll();

            // 3. Usuwanie wszystkich zadań
            System.out.println("Usuwanie wszystkich zadań...");
            taskRepository.deleteAll();

            // 4. Usuwanie wszystkich raportów (zachowujemy typy raportów)
            System.out.println("Usuwanie wszystkich raportów...");
            reportRepository.deleteAll();

            // 5. Usuwanie wszystkich członków zespołów
            System.out.println("Usuwanie wszystkich członków zespołów...");
            teamMemberRepository.deleteAll();

            // 6. Usuwanie wszystkich zespołów
            System.out.println("Usuwanie wszystkich zespołów...");
            teamRepository.deleteAll();

            // 7. Usuwanie wszystkich użytkowników oprócz admina
            System.out.println("Usuwanie użytkowników (zachowując 'admin')...");
            userRepository.findAll().forEach(user -> {
                if (!ADMIN_USERNAME.equals(user.getUsername())) {
                    userRepository.delete(user);
                }
            });

            // 8. Usuwanie wszystkich typów raportów oprócz podstawowych
            System.out.println("Usuwanie niestandardowych typów raportów...");
            reportTypeRepository.findAll().forEach(reportType -> {
                if (!BASIC_REPORT_TYPES.contains(reportType.getName())) {
                    reportTypeRepository.delete(reportType);
                }
            });

            // 9. Usuwanie wszystkich priorytetów oprócz podstawowych
            System.out.println("Usuwanie niestandardowych priorytetów...");
            priorityRepository.findAll().forEach(priority -> {
                if (!BASIC_PRIORITIES.contains(priority.getName())) {
                    priorityRepository.delete(priority);
                }
            });

            // 10. Usuwanie wszystkich statusów zadań oprócz podstawowych
            System.out.println("Usuwanie niestandardowych statusów zadań...");
            taskStatusRepository.findAll().forEach(status -> {
                if (!BASIC_STATUSES.contains(status.getName())) {
                    taskStatusRepository.delete(status);
                }
            });

            // 11. Usuwanie wszystkich ustawień systemowych oprócz podstawowych
            System.out.println("Usuwanie niestandardowych ustawień systemowych...");
            systemSettingRepository.findAll().forEach(setting -> {
                if (!BASIC_SYSTEM_SETTING_KEY.equals(setting.getKey())) {
                    systemSettingRepository.delete(setting);
                }
            });

            System.out.println("Czyszczenie danych zakończone. W bazie danych pozostały tylko podstawowe dane z 04-insert-basic-data.xml.");
        };
    }
}