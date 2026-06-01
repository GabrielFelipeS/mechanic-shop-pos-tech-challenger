package org.project.mechanic_shop.infrastructure.http;

public interface EmailService {
	void sendEmail(String[] to, String subject, String body);
}
