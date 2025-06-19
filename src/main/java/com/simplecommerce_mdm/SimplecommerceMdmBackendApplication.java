package com.simplecommerce_mdm;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimplecommerceMdmBackendApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SimplecommerceMdmBackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 SimpleCommerce MDM Backend started successfully!");
        System.out.println("📊 Access H2 Console: http://localhost:8080/h2-console");
        System.out.println("❤️  Health Check: http://localhost:8080/api/v1/health");
    }
}
