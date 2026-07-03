package org.project.mechanic_shop.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.project.mechanic_shop.config.AbstractIntegrationTest;
import org.project.mechanic_shop.domain.dto.user_dto.UserManDto;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.enums.UserRoleEnum;
import org.project.mechanic_shop.application.ports.UserRepositoryPort;
import org.project.mechanic_shop.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class UserControllerIT extends AbstractIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private UserRepositoryPort userRepository;

	@Test
	void shouldCreateUserAndPersistIt() throws Exception {
		UserManDto payload = buildUserManDto(
			"52998224725",
			"Integration Create",
			"integration.create@test.com",
			"11999990001"
		);

		String responseBody = mockMvc
			.perform(
				post("/api/users/create")
					.with(AuthUtil.admin())
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(payload))
			)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
			.andExpect(jsonPath("$.message").value("success"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		JsonNode responseJson = objectMapper.readTree(responseBody);
		UUID externalId = UUID.fromString(responseJson.get("data").asText());

		User persistedUser = userRepository.findByExternalId(externalId).orElseThrow();

		assertThat(persistedUser.getName()).isEqualTo(payload.name());
		assertThat(persistedUser.getEmail()).isEqualTo(payload.email());
		assertThat(persistedUser.getDocument()).isEqualTo(payload.document());
		assertThat(persistedUser.getRole()).isEqualTo(payload.role().name());
		assertThat(persistedUser.getActive()).isEqualTo(payload.active());
		assertThat(persistedUser.getPhone()).isEqualTo(payload.phone());
	}

	@Test
	void shouldReturnValidationErrorWhenCreatePayloadIsInvalid() throws Exception {
		UserManDto invalidPayload = new UserManDto("", "", "invalid-email", null, null, "", "");

		mockMvc
			.perform(
				post("/api/users/create")
					.with(AuthUtil.admin())
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(invalidPayload))
			)
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_CONTENT.value()))
			.andExpect(jsonPath("$.message").value("Validation error."))
			.andExpect(jsonPath("$.errors").isArray());
	}

	@Test
	void shouldFindUserByExternalIdUsingRealRepository() throws Exception {
		User savedUser = userRepository.save(
			buildUser("11144477735", "Integration Find", "integration.find@test.com", "11999990002")
		);

		mockMvc
			.perform(get("/api/users/{id}", savedUser.getExternalId()).with(AuthUtil.admin()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.data.externalId").value(savedUser.getExternalId().toString()))
			.andExpect(jsonPath("$.data.name").value(savedUser.getName()))
			.andExpect(jsonPath("$.data.email").value(savedUser.getEmail()))
			.andExpect(jsonPath("$.data.document").value(savedUser.getDocument()));
	}

	@Test
	void shouldUpdateUserUsingRealServiceAndRepository() throws Exception {
		User savedUser = userRepository.save(
			buildUser("16899535009", "Integration Before Update", "integration.update.before@test.com", "11999990003")
		);

		UserManDto updatePayload = new UserManDto(
			"39053344705",
			"Integration After Update",
			"integration.update.after@test.com",
			UserRoleEnum.MECHANIC,
			false,
			"Updated@123",
			"11999990004"
		);

		mockMvc
			.perform(
				put("/api/users/{id}", savedUser.getExternalId())
					.with(AuthUtil.admin())
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(updatePayload))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.data.externalId").value(savedUser.getExternalId().toString()))
			.andExpect(jsonPath("$.data.name").value(updatePayload.name()))
			.andExpect(jsonPath("$.data.email").value(updatePayload.email()))
			.andExpect(jsonPath("$.data.document").value(updatePayload.document()))
			.andExpect(jsonPath("$.data.active").value(updatePayload.active()))
			.andExpect(jsonPath("$.data.phone").value(updatePayload.phone()));

		User updatedUser = userRepository.findByExternalId(savedUser.getExternalId()).orElseThrow();

		assertThat(updatedUser.getName()).isEqualTo(updatePayload.name());
		assertThat(updatedUser.getEmail()).isEqualTo(updatePayload.email());
		assertThat(updatedUser.getDocument()).isEqualTo(updatePayload.document());
		assertThat(updatedUser.getActive()).isEqualTo(updatePayload.active());
		assertThat(updatedUser.getPhone()).isEqualTo(updatePayload.phone());
		assertThat(updatedUser.getRole()).isEqualTo(savedUser.getRole());
	}

	@Test
	void shouldSearchUsersUsingPersistedData() throws Exception {
		User savedUser = userRepository.save(
			buildUser("45317828791", "Integration Search", "integration.search@test.com", "11999990005")
		);

		mockMvc
			.perform(get("/api/users/search").with(AuthUtil.admin()).param("document", savedUser.getDocument()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("success"))
			.andExpect(jsonPath("$.data.content[0].externalId").value(savedUser.getExternalId().toString()))
			.andExpect(jsonPath("$.data.content[0].name").value(savedUser.getName()))
			.andExpect(jsonPath("$.data.content[0].email").value(savedUser.getEmail()))
			.andExpect(jsonPath("$.data.content[0].document").value(savedUser.getDocument()));
	}

	@Test
	void shouldListAvailableRolesWithoutAdmin() throws Exception {
		String responseBody = mockMvc
			.perform(get("/api/users/available-roles").with(AuthUtil.admin()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("success"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		JsonNode roles = objectMapper.readTree(responseBody).get("data");

		assertThat(roles).isNotNull();
		assertThat(roles.size()).isEqualTo(UserRoleEnum.values().length - 1);
		assertThat(roles.toString()).doesNotContain("\"name\":\"AuthUtil.admin\"");
		assertThat(roles.toString()).contains("\"name\":\"RECEPTIONIST\"");
	}

	private UserManDto buildUserManDto(String document, String name, String email, String phone) {
		return new UserManDto(document, name, email, UserRoleEnum.RECEPTIONIST, true, "Secret@123", phone);
	}

	private User buildUser(String document, String name, String email, String phone) {
		User user = new User();
		user.setDocument(document);
		user.setName(name);
		user.setEmail(email);
		user.setRole(UserRoleEnum.RECEPTIONIST.name());
		user.setActive(true);
		user.setPassword("Secret@123");
		user.setPhone(phone);
		return user;
	}
}
