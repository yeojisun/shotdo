package com.shotdo.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        System.out.println("=== STARTING SHOTDO BACKEND APPLICATION ===");
        System.out.println("SPRING_PROFILES_ACTIVE: " + System.getenv("SPRING_PROFILES_ACTIVE"));
        System.out.println("DB_HOST: " + System.getenv("DB_HOST"));
        System.out.println("DB_NAME: " + System.getenv("DB_NAME"));
        System.out.println("DB_USERNAME: " + System.getenv("DB_USERNAME"));
        System.out.println("DB_PASSWORD is set: " + (System.getenv("DB_PASSWORD") != null && !System.getenv("DB_PASSWORD").isEmpty()));
        System.out.println("===========================================");
        SpringApplication.run(BackendApplication.class, args);
    }
}
