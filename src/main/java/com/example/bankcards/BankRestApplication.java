package com.example.bankcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the <b>Bank Cards REST API</b> application.
 * <p>
 * This class bootstraps the Spring Boot application, initializing
 * all configuration components, security settings, and API endpoints.
 * </p>
 *
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Launches the embedded Spring Boot application context.</li>
 *   <li>Scans and registers all components under {@code com.example.bankcards.*}.</li>
 *   <li>Serves as the main entry point for running the backend service.</li>
 * </ul>
 *
 * <p>
 * The application provides a secure REST API for user authentication,
 * card management, and administrative operations.
 * </p>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * $ mvn spring-boot:run
 * }</pre>
 * or
 * <pre>{@code
 * $ java -jar bankcards.jar
 * }</pre>
 *
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 *
 * @author Konstantin Sakhokiia
 * @since 1.0
 */
@SpringBootApplication
public class BankRestApplication {

    /**
     * Main method used to start the Spring Boot application.
     *
     * @param args optional runtime arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(BankRestApplication.class, args);
    }
}
