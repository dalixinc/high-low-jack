package com.dalegames.highlowjack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application for High Low Jack card game.
 *
 * @author Dale &amp; Primus
 * @version 1.0
 */
@SpringBootApplication(scanBasePackages = "com.dalegames.highlowjack")
public class HighLowJackApplication {

    /**
     * Main entry point.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(HighLowJackApplication.class, args);
    }
}
