package com.simplecommerce_mdm.email.service;

import java.util.Map;

public interface EmailService {

    /**
     * Sends a simple text-based email.
     *
     * @param to          The recipient's email address.
     * @param subject     The subject of the email.
     * @param textContent The plain text content of the email.
     */
    void sendSimpleMessage(String to, String subject, String textContent);

    /**
     * Sends an email using a pre-defined Brevo template.
     *
     * @param to             The recipient's email address.
     * @param templateId     The ID of the template on the Brevo platform.
     * @param templateParams A map of parameters to populate the template.
     */
    void sendEmailWithTemplate(String to, Long templateId, Map<String, Object> templateParams);
}