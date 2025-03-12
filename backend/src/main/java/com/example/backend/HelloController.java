package com.example.backend;

import org.springframework.web.bind.annotation.*;

/**
 * A simple REST controller that handles HTTP requests for the home endpoints.
 * This controller provides basic functionality.
 *
 * @author Karol/Kacper
 * @version 1.0
 * @since 2025-03-12
 */
@RestController
@RequestMapping("/home")
public class HelloController {

    /**
     * Returns a simple message.
     * This endpoint responds to GET requests at the /home/hello path.
     *
     * @return a String
     */
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from Spring Boot!";
    }
}