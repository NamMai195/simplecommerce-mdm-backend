package com.simplecommerce_mdm.email.controller;

import com.simplecommerce_mdm.email.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test-email")
@RequiredArgsConstructor
@Profile("dev") // IMPORTANT: This controller is only active in the 'dev' profile
@Tag(name = "99. Test Email", description = "Endpoints for testing email functionality (DEV only)")
public class TestEmailController {

    private final EmailService emailService;

    @PostMapping("/simple")
    @Operation(summary = "Send a simple test email",
               description = "Sends a plain text email to the specified address. Only for development/testing.")
    public ResponseEntity<String> sendSimpleEmail(@RequestBody TestEmailRequest request) {
        emailService.sendSimpleMessage(request.to(), request.subject(), request.content());
        return ResponseEntity.ok("Simple test email sent successfully to " + request.to());
    }

    @PostMapping("/template")
    @Operation(summary = "Send a template-based test email",
               description = "Sends an email using a Brevo template. Only for development/testing.")
    public ResponseEntity<String> sendTemplateEmail(@RequestBody TestTemplateEmailRequest request) {
        emailService.sendEmailWithTemplate(request.to(), request.templateId(), request.params());
        return ResponseEntity.ok("Template test email sent successfully to " + request.to());
    }

    // --- DTOs for request bodies ---

    record TestEmailRequest(String to, String subject, String content) {}

    record TestTemplateEmailRequest(String to, Long templateId, Map<String, Object> params) {}
} 