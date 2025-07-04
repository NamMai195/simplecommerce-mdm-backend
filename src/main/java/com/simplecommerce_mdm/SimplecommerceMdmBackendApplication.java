package com.simplecommerce_mdm;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.util.TimeZone;

@SpringBootApplication
public class SimplecommerceMdmBackendApplication implements CommandLineRunner {

    @Autowired
    private Environment environment;

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
        System.out.println("🕒 Start time: " + LocalDateTime.now());

        // 👇 Log thông tin PostgreSQL
        String dbUser = environment.getProperty("spring.datasource.username");
        String dbPass = environment.getProperty("spring.datasource.password");

        System.out.println("🛠️  PostgreSQL Username: " + dbUser);
        System.out.println("🔐 PostgreSQL Password: " + dbPass);
    }
}
