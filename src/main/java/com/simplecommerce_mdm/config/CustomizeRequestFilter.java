package com.simplecommerce_mdm.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simplecommerce_mdm.auth.service.JwtService;
import com.simplecommerce_mdm.auth.service.UserServiceDetail;
import com.simplecommerce_mdm.common.enums.TokenType;
import com.simplecommerce_mdm.exception.TokenValidationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.Date;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "CUSTOMIZE-FILTER")
public class CustomizeRequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserServiceDetail serviceDetail;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // --- TEMPORARY DEBUG LOGGING ---
        System.out.println("--- RECEIVED REQUEST TO: " + request.getRequestURI() + " ---");
        java.util.Collections.list(request.getHeaderNames())
            .forEach(headerName -> 
                System.out.println(headerName + ": " + request.getHeader(headerName))
            );
        System.out.println("--- END OF HEADERS ---");
        // --- END OF DEBUG LOGGING ---
        log.info("{} {}", request.getMethod(), request.getRequestURI());

        try {
            final String authHeader = request.getHeader(AUTHORIZATION);
            if (StringUtils.hasLength(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                log.info("token: {}...", token.substring(0, Math.min(token.length(), 20)));

                String email = jwtService.extractEmail(token, TokenType.ACCESS_TOKEN);
                log.info("username: {}", email);

                UserDetails user = serviceDetail.loadUserByUsername(email);

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                    UserDetails authenticatedUser = (UserDetails) authentication.getPrincipal();
                    log.info("Authenticated user: {}", authenticatedUser.getUsername());
                }
            }
            filterChain.doFilter(request, response);
        } catch (AccessDeniedException | TokenValidationException e) {
            log.error("Authentication error: {}", e.getMessage());
            response.setStatus(FORBIDDEN.value());
            response.setContentType(APPLICATION_JSON_VALUE);
            response.getWriter().write(errorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error in filter: {}", e.getMessage());
            response.setStatus(INTERNAL_SERVER_ERROR.value());
            response.setContentType(APPLICATION_JSON_VALUE);
            response.getWriter().write(errorResponse("Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Create error response with pretty template
     * @param message
     * @return
     */
    private String errorResponse(String message) {
        try {
            ErrorResponse error = new ErrorResponse();
            error.setTimestamp(new Date());
            error.setError(FORBIDDEN.name());
            error.setStatus(FORBIDDEN.value());
            error.setMessage(message);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(error);
        } catch (Exception e) {
            log.error("Error serializing error response", e);
            return "{\"error\": \"Internal Server Error\", \"message\": \"Failed to generate error response\"}";
        }
    }

    @Setter
    @Getter
    private class ErrorResponse {
        private Date timestamp;
        private int status;
        private String error;
        private String message;
    }
}
