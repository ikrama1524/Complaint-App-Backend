package com.civiccomplaint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for Civic Complaint Management System.
 * 
 * @author Civic Complaint Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class CivicComplaintApplication {

    public static void main(String[] args) {
        SpringApplication.run(CivicComplaintApplication.class, args);
    }
}
