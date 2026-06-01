package org.project.mechanic_shop.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.project.mechanic_shop.domain.dto.login_dto.LoginDto;
import org.project.mechanic_shop.infrastructure.repositories.UserRepository;
import org.project.mechanic_shop.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIT {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private UserRepository userRepository;

	@Test
	void shouldBeAbleLogin() throws Exception {
		LoginDto payload = new LoginDto("warehouse@gmail.com", "TESTE123");

		mockMvc
			.perform(
				post("/api/auth/login")
					.with(AuthUtil.admin())
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(payload))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
	}

	@Test
	void shouldBeNotAbleLogin() throws Exception {
		LoginDto payload = new LoginDto("email@does_not_exists.com", "TESTE123");

		mockMvc
			.perform(
				post("/api/auth/login")
					.with(AuthUtil.admin())
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(payload))
			)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));
	}
}
