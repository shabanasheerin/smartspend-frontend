package com.smartspend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for SmartSpend AI - Enterprise Expense Tracker SaaS.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SmartSpendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartSpendApplication.class, args);
    }
}
