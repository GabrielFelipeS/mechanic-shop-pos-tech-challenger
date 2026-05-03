package org.project.mechanic_shop.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.project.mechanic_shop.config.ObjectMapperConfig;
import org.project.mechanic_shop.config.SecurityConfig;
import org.project.mechanic_shop.dto.user_dto.UserManDto;
import org.project.mechanic_shop.mappers.UserMapperImpl;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.services.UserService;
import org.project.mechanic_shop.utils.UserHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ SecurityConfig.class, UserMapperImpl.class, ObjectMapperConfig.class })
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService service;

	@Nested
	class FindById {

		@Test
		void shouldFindUserByExternalId() throws Exception {
			var externalId = UUID.randomUUID();
			var user = UserHelper.generateUser();

			when(service.findByExternalId(externalId)).thenReturn(user);

			mockMvc
				.perform(get(String.format("/api/users/%s", externalId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(200))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data.name").value("TESTE"));
		}

		@Test
		void shouldNotFoundUserByExternalId() throws Exception {
			var externalId = UUID.randomUUID();

			when(service.findByExternalId(externalId)).thenThrow(EntityNotFoundException.class);

			mockMvc.perform(get(String.format("/api/users/%s", externalId))).andExpect(status().isNotFound());
		}
	}

	@Nested
	class Create {

		@Test
		void shouldBeCreateUser() throws Exception {
			var user = UserHelper.generateUser();

			UserManDto userDto = UserHelper.generateUserMenDto();

			when(service.create(any(User.class))).thenReturn(user);

			mockMvc
				.perform(
					post("/api/users/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDto))
				)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data").value(user.getExternalId()));
		}

		@Test
		void shouldThrowIllegalArgumentException() throws Exception {
			UserManDto userDto = UserHelper.generateUserMenDto();

			when(service.create(any(User.class))).thenThrow(IllegalArgumentException.class);

			mockMvc
				.perform(
					post("/api/users/create")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDto))
				)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));
		}
	}

	@Nested
	class Update {

		@Test
		void shouldBeAbleUpdateUser() throws Exception {
			var externalId = UUID.randomUUID();
			var user = UserHelper.generateUser();

			UserManDto userDto = UserHelper.generateUserMenDto();

			when(service.update(eq(externalId), any(User.class))).thenReturn(user);

			mockMvc
				.perform(
					put("/api/users/{id}", externalId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDto))
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("success"));

			verify(service).update(eq(externalId), any(User.class));
		}

		@Test
		void shouldThrowIllegalArgumentException() throws Exception {
			var externalId = UUID.randomUUID();

			UserManDto userDto = UserHelper.generateUserMenDto();

			when(service.update(eq(externalId), any(User.class))).thenThrow(IllegalArgumentException.class);

			mockMvc
				.perform(
					put("/api/users/{id}", externalId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDto))
				)
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()));
		}
	}

	@Nested
	class Search {

		@Test
		void shouldSearchWithPageable() throws Exception {
			var user = UserHelper.generateUser();
			var listUser = List.of(user);
			ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

			Page<User> page = new PageImpl<>(listUser, PageRequest.of(0, 2), 1);

			when(
				service.search(
					eq(user.getDocument()),
					eq(user.getName()),
					eq(user.getEmail()),
					eq(user.getRole()),
					any(Pageable.class)
				)
			).thenReturn(page);

			mockMvc
				.perform(
					get("/api/users/search")
						.param("document", user.getDocument())
						.param("name", user.getName())
						.param("email", user.getEmail())
						.param("role", user.getRole())
						.param("page", "0")
						.param("size", "10")
						.param("sort", "name,asc")
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data.content").isArray())
				.andExpect(jsonPath("$.data.content[0].name").value(user.getName()));

			verify(service).search(
				eq(user.getDocument()),
				eq(user.getName()),
				eq(user.getEmail()),
				eq(user.getRole()),
				captor.capture()
			);

			Pageable pageableCaptured = captor.getValue();

			assertThat(pageableCaptured.getPageNumber()).isZero();
			assertThat(pageableCaptured.getPageSize()).isEqualTo(10);
			assertThat(pageableCaptured.getSort().getOrderFor("name").isAscending()).isTrue();
		}

		@Test
		void shouldSearchWithPageableDefault() throws Exception {
			var user = UserHelper.generateUser();
			var listUser = List.of(user);
			ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

			Page<User> page = new PageImpl<>(listUser, PageRequest.of(0, 2), 1);

			when(service.search(eq(user.getDocument()), any(), any(), any(), any(Pageable.class))).thenReturn(page);

			mockMvc
				.perform(get("/api/users/search").param("document", user.getDocument()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.data.content").isArray())
				.andExpect(jsonPath("$.data.content[0].name").value(user.getName()));

			verify(service).search(eq(user.getDocument()), eq(null), eq(null), eq(null), captor.capture());

			Pageable pageableCaptured = captor.getValue();
			pageableCaptured.getSort().forEach(System.out::println);
			assertThat(pageableCaptured.getPageNumber()).isZero();
			assertThat(pageableCaptured.getPageSize()).isEqualTo(10);
			assertThat(pageableCaptured.getSort().getOrderFor("createdAt").isDescending()).isTrue();
		}
	}

	@Nested
	class GetAvailableRoles {

		@Test
		void shouldBeAbleUpdateUser() throws Exception {
			mockMvc
				.perform(get("/api/users/available-roles"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
				.andExpect(jsonPath("$.message").value("success"));
		}
	}
}
