package org.project.mechanic_shop.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.mechanic_shop.application.ports.EmailService;
import org.project.mechanic_shop.infrastructure.http.impl.SmtpEmailServiceImpl;
import org.springframework.mail.MailSendException;
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
	void shouldRetryAndEventuallyThrowWhenSendingKeepsFailing() {
		doThrow(new MailSendException("smtp failure")).when(mailSender).send(any(SimpleMailMessage.class));

		assertThatThrownBy(() -> service.sendEmail(new String[] { "a@test.com" }, "Assunto", "Corpo"))
			.isInstanceOf(IllegalStateException.class)
			.hasCauseInstanceOf(MailSendException.class);

		verify(mailSender, times(3)).send(any(SimpleMailMessage.class));
	}

	@Test
	void shouldRecoverOnRetryAfterATransientFailure() {
		doThrow(new MailSendException("transient failure"))
			.doNothing()
			.when(mailSender)
			.send(any(SimpleMailMessage.class));

		assertThatCode(() ->
			service.sendEmail(new String[] { "a@test.com" }, "Assunto", "Corpo")
		).doesNotThrowAnyException();

		verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
	}
}
