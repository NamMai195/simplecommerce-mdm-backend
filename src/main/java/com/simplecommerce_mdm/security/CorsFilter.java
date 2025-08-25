package com.simplecommerce_mdm.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CorsFilter implements Filter {

    @Value("${frontend.admin-seller.url:http://localhost:3000}")
    private String adminSellerFrontendUrl;

    @Value("${frontend.user.url:http://localhost:3001}")
    private String userFrontendUrl;

    @Value("${frontend.url:http://localhost:5173}")
    private String defaultFrontendUrl;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // Tạo danh sách tất cả frontend URLs được phép
        List<String> allowedOrigins = Arrays.asList(
                adminSellerFrontendUrl,
                userFrontendUrl,
                defaultFrontendUrl,
                // Vercel frontends
                "https://simplecommerce-mdm-as-frontend.vercel.app",
                "https://simplecommerce-user-frontend.vercel.app",
                "https://simplecommerce-frontend.vercel.app",
                // Custom domain
                "https://sc-mdm-api.nammai.id.vn",
                // Localhost for development
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:5173"
        );

        String origin = request.getHeader("Origin");
        if (origin != null && allowedOrigins.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }

        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, X-XSRF-TOKEN");
        response.setHeader("Access-Control-Expose-Headers", "Authorization, Content-Type, Content-Length");

        // Handle preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization needed
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
