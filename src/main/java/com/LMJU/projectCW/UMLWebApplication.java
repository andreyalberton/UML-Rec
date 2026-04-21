package com.LMJU.projectCW;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the whole application.
 * Spring Boot initiate all the controllers and wiring automatically.
 * Once this boots up, the web UI is at http://localhost:8080
 */
@SpringBootApplication
public class UMLWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(UMLWebApplication.class, args);
        System.out.println("      UML WEB INTERFACE READY AT:     ");
        System.out.println("  http://localhost:8080               ");
        System.out.println("--------------------------------------");
    }
}