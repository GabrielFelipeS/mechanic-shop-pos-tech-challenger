package org.project.mechanic_shop.infrastructure.http.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.infrastructure.http.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "SMTP_ACTIVE", havingValue = "true")
public class SmtpEmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    private static final Lock EMAIL_LOCK = new ReentrantLock();

    @Override
    public void sendEmail(String[] to, String subject, String body) {
        log.info("Preparing to send real email to {} users. Thread: {}", to.length, Thread.currentThread().getName());


        EMAIL_LOCK.lock();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            log.info("Sending email to: {}...", to.length);
            mailSender.send(message);
            log.info("Email successfully sent to: {}", (Object) to);

        } catch (Exception e) {
            log.error("Failed to send email to [{}]. Error: {}", to, e.getMessage());
        } finally {
            EMAIL_LOCK.unlock();
        }
    }

}
