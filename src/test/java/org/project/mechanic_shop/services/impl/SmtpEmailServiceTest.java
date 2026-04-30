package org.project.mechanic_shop.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.mechanic_shop.services.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

	private EmailService service;

	@Mock
	private JavaMailSender mailSender;

	@BeforeEach
	void setup() {
		service = new SmtpEmailServiceImpl(mailSender);
		ReflectionTestUtils.setField(service, "senderEmail", "system@mechanicshop.com");
	}

	@Test
	void shouldBuildAndSendEmail() {
		String[] recipients = { "a@test.com", "b@test.com" };
		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

		service.sendEmail(recipients, "Assunto", "Corpo");

		verify(mailSender).send(captor.capture());

		SimpleMailMessage message = captor.getValue();
		assertThat(message.getFrom()).isEqualTo("system@mechanicshop.com");
		assertThat(message.getTo()).containsExactly(recipients);
		assertThat(message.getSubject()).isEqualTo("Assunto");
		assertThat(message.getText()).isEqualTo("Corpo");
	}

	@Test
	void shouldSwallowExceptionWhenSendingFails() {
		doThrow(new RuntimeException("smtp failure"))
			.when(mailSender)
			.send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

		assertThatCode(() ->
			service.sendEmail(new String[] { "a@test.com" }, "Assunto", "Corpo")
		).doesNotThrowAnyException();
	}
}
