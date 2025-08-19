package com.simplecommerce_mdm.security;

import com.simplecommerce_mdm.config.CustomizeRequestFilter;
import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Quan trọng để dùng @PreAuthorize/@PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${frontend.url}")
    private String frontendUrl;

    private final CustomizeRequestFilter customizeRequestFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(customizeRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-ui.html",
                                "/api/v1/categories/**",
                                "/api/v1/products/**", // Public product endpoints
                                "/api/v1/shops/**", // Public shop endpoints
                                "/api/v1/test-email/**",
                                "/api/v1/test/**",
                                "/h2-console/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // API Admin yêu cầu quyền ADMIN
                        .requestMatchers("/api/v1/seller/**").hasRole("SELLER") // API Seller yêu cầu quyền SELLER
                        .requestMatchers("/api/v1/user/**").hasAnyRole("USER", "SELLER", "ADMIN") // API User cho tất cả user
                        // Quyền truy cập cho user profile
                        .requestMatchers("/api/v1/users/profile").hasAnyRole("USER", "SELLER", "ADMIN")
                        .anyRequest().authenticated() // Tất cả các request khác đều cần xác thực
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())); // for H2 console

        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(frontendUrl)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

}