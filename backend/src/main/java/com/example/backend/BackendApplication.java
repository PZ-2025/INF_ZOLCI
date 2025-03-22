package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import io.github.cdimascio.dotenv.Dotenv;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.database.repository")
@EntityScan(basePackages = "com.example.database.models")
@ComponentScan(basePackages = {
        "com.example.database",
        "com.example.database.controllers",
        "com.example.database.services"
})

public class BackendApplication {
    public static void main(String[] args)
    {
        // Ładowanie zmiennych środowiskowych
        Dotenv dotenv = Dotenv.configure().load();

        // Ustaw je jako właściwości systemowe
        dotenv.entries().forEach(e ->
                System.setProperty(e.getKey(), e.getValue())
        );


        SpringApplication.run(BackendApplication.class, args);
    }
}
