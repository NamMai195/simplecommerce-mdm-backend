package com.simplecommerce_mdm.email.service.impl;

import com.simplecommerce_mdm.email.service.EmailService;
import com.simplecommerce_mdm.exception.EmailSendingException;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

@Service
@Slf4j
public class BrevoEmailService implements EmailService {

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private TransactionalEmailsApi apiInstance;

    @PostConstruct
    public void init() {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(apiKey);
        apiInstance = new TransactionalEmailsApi(defaultClient);
    }

    @Override
    public void sendSimpleMessage(String to, String subject, String textContent) {
        SendSmtpEmail sendSmtpEmail = createBaseEmail(to);
        sendSmtpEmail.setSubject(subject);
        sendSmtpEmail.setTextContent(textContent);

        try {
            CreateSmtpEmail result = apiInstance.sendTransacEmail(sendSmtpEmail);
            log.info("Email sent successfully to {}. Message ID: {}", to, result.getMessageId());
        } catch (ApiException e) {
            log.error("Error sending email to {}: {}", to, e.getResponseBody(), e);
            throw new EmailSendingException("Failed to send simple email to " + to, e);
        }
    }

    @Override
    public void sendEmailWithTemplate(String to, Long templateId, Map<String, Object> templateParams) {
        SendSmtpEmail sendSmtpEmail = createBaseEmail(to);
        sendSmtpEmail.setTemplateId(templateId);

        if (templateParams != null && !templateParams.isEmpty()) {
            Properties params = new Properties();
            params.putAll(templateParams);
            sendSmtpEmail.setParams(params);
        }

        try {
            CreateSmtpEmail result = apiInstance.sendTransacEmail(sendSmtpEmail);
            log.info("Template email sent successfully to {}. Message ID: {}", to, result.getMessageId());
        } catch (ApiException e) {
            log.error("Error sending template email to {}: {}", to, e.getResponseBody(), e);
            throw new EmailSendingException("Failed to send template email to " + to, e);
        }
    }

    private SendSmtpEmail createBaseEmail(String to) {
        SendSmtpEmail email = new SendSmtpEmail();
        email.setSender(new SendSmtpEmailSender().email(senderEmail).name(senderName));
        email.setTo(Collections.singletonList(new SendSmtpEmailTo().email(to)));
        return email;
    }
}