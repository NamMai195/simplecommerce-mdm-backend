package com.simplecommerce_mdm;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.TimeZone;

@SpringBootApplication
public class SimplecommerceMdmBackendApplication implements CommandLineRunner {

    @PostConstruct
    void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    public static void main(String[] args) {
        SpringApplication.run(SimplecommerceMdmBackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 SimpleCommerce MDM Backend started successfully!");
        System.out.println("📊 Access H2 Console: http://localhost:8080/h2-console");
        System.out.println("❤️  Health Check: http://localhost:8080/api/v1/health");
        System.out.println(LocalDateTime.now());
    }
}
