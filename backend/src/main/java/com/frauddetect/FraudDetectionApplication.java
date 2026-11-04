package com.frauddetect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Fraud Detection System - Main Application Entry Point
 * 
 * AI-powered transaction fraud detection using ML algorithms including:
 * - Velocity-based anomaly detection
 * - Statistical deviation scoring
 * - Geo-location mismatch analysis
 * - Merchant risk categorization
 * - Behavioral pattern recognition
 */
@SpringBootApplication
public class FraudDetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(FraudDetectionApplication.class, args);
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║     FRAUD DETECTION SYSTEM - RUNNING ON 8080    ║");
        System.out.println("║     Domain: Finance & Security                   ║");
        System.out.println("║     Technology: Java + ML + REST APIs            ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");
    }

    /**
     * CORS configuration to allow requests from the frontend web interface
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
