package org.project.mechanic_shop.services.impl;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.project.mechanic_shop.services.EmailService;

class MockEmailServiceTest {

	private final EmailService service = new MockEmailServiceImpl();

	@Test
	void shouldNotThrowWhenMockEmailIsSent() {
		assertThatCode(() ->
			service.sendEmail(new String[] { "a@test.com" }, "Assunto", "Corpo")
		).doesNotThrowAnyException();
	}
}
