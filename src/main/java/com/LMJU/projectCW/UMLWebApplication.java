package com.LMJU.projectCW;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UMLWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(UMLWebApplication.class, args);
        System.out.println("  UML WEB INTERFACE READY AT:         ");
        System.out.println("  http://localhost:8080               ");
        System.out.println("--------------------------------------");
    }
}