package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test integracyjny do sprawdzenia poprawności połączenia z bazą danych.
 * <p>
 * Test weryfikuje czy:
 * 1. Kontekst Spring poprawnie się ładuje
 * 2. Źródło danych (DataSource) jest dostępne
 * 3. Możliwe jest nawiązanie połączenia z bazą danych
 * 4. Baza danych jest dostępna i można wykonywać na niej zapytania
 * <p>
 * Test używa profilu 'test', który powinien być skonfigurowany do pracy z bazą H2 w pamięci.
 *
 * @author Jakub
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("deploy")
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Test sprawdzający czy DataSource jest poprawnie skonfigurowany i można nawiązać
     * połączenie z bazą danych.
     */
    @Test
    void dataSourceConfigured() throws SQLException {
        assertNotNull(dataSource, "DataSource powinien być skonfigurowany");

        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(1000), "Połączenie z bazą danych powinno być aktywne");
            assertFalse(connection.isClosed(), "Połączenie nie powinno być zamknięte");
        }
    }

    /**
     * Test sprawdzający czy możliwe jest wykonanie prostego zapytania SQL na bazie danych.
     */
    @Test
    void canExecuteSimpleQuery() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertEquals(1, result, "Powinno być możliwe wykonanie prostego zapytania SQL");
    }

    /**
     * Test sprawdzający czy baza danych H2 jest poprawnie skonfigurowana
     * poprzez wykonanie zapytania specyficznego dla H2.
     */
    @Test
    void h2DatabaseIsRunning() throws SQLException {
        String dbProductName = jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductName();
        System.out.printf("Baza danych: %s%n", dbProductName);


        assertNotNull(dbProductName, "Powinno być możliwe wykonanie zapytania specyficznego dla H2");
        assertTrue(dbProductName.contains("H2"), "Baza danych powinna być H2");
    }
}