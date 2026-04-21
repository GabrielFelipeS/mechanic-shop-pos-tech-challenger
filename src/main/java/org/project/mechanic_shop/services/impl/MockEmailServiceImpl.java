package org.project.mechanic_shop.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.services.EmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "SMTP_ACTIVE", havingValue = "false", matchIfMissing = true)
public class MockEmailServiceImpl implements EmailService {

    @Override
    public void sendEmail(String[] to, String subject, String body) {
        log.info("=================================================");
        log.info("[MOCK EMAIL DISPATCHER]");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Body: {}", body);
        log.info("Status: SUCCESS (Console Print Only)");
        log.info("=================================================");
    }
}