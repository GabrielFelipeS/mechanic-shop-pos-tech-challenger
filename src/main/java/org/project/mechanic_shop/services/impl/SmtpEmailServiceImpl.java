package org.project.mechanic_shop.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.services.EmailService;
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

	@Override
	public void sendEmail(String[] to, String subject, String body) {
		log.info("Preparing to send real email to {} users", to.length);

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			String senderEmail = "sistema@mechanicshop.com";
			message.setFrom(senderEmail);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);

			mailSender.send(message);
			log.info("Email successfully sent to {} users", to.length);
		} catch (Exception e) {
			log.error("Failed to send email to {}. Error: {}", to, e.getMessage());
		}
	}
}
