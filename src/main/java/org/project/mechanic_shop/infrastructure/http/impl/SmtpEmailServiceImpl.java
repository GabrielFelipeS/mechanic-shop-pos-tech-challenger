package org.project.mechanic_shop.infrastructure.http.impl;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.application.ports.EmailService;
import org.project.mechanic_shop.shared.config.observability.ObservabilityReporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "SMTP_ACTIVE", havingValue = "true")
public class SmtpEmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final ObservabilityReporter observabilityReporter;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BACKOFF_MS = 200;

    @Override
    public void sendEmail(String[] to, String subject, String body) {
        log.info("Preparing to send real email to {} users. Thread: {}", to.length, Thread.currentThread().getName());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        Exception lastFailure = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                mailSender.send(message);
                log.info("Email successfully sent to: {}", (Object) to);
                return;
            } catch (Exception e) {
                lastFailure = e;
                log.warn(
                    "Attempt {}/{} to send email to {} failed: {}",
                    attempt,
                    MAX_ATTEMPTS,
                    Arrays.toString(to),
                    e.getMessage()
                );
                if (attempt < MAX_ATTEMPTS) sleepBeforeRetry();
            }
        }

        IllegalStateException failure = new IllegalStateException(
            "Failed to send email to " + Arrays.toString(to) + " after " + MAX_ATTEMPTS + " attempts",
            lastFailure
        );

        observabilityReporter.recordIntegrationFailure("SMTP", "sendEmail", failure);

        throw failure;
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_BACKOFF_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
