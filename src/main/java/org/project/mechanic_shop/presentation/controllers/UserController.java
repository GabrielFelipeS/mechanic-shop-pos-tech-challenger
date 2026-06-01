package org.project.mechanic_shop.presentation.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.domain.dto.responses.ApiResponse;
import org.project.mechanic_shop.domain.dto.user_dto.RoleOptionDto;
import org.project.mechanic_shop.domain.dto.user_dto.UserDto;
import org.project.mechanic_shop.domain.dto.user_dto.UserManDto;
import org.project.mechanic_shop.application.mappers.UserMapper;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.enums.UserRoleEnum;
import org.project.mechanic_shop.application.services.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management")
@Slf4j
public class UserController {

	private final UserService service;
	private final UserMapper mapper;

	private static final String SUCCESS_MESSAGE = "success";

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
	public ResponseEntity<ApiResponse> findById(@PathVariable UUID id) {
		log.info("Find user by External ID: {}", id);

		var user = service.findByExternalId(id);
		var dto = mapper.toDto(user);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, dto));
	}

	@PostMapping("/create")
	@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
	public ResponseEntity<ApiResponse> create(@RequestBody @Valid UserManDto dto) {
		log.info("Try create user with parameters: {}", dto);

		User user = mapper.toEntity(dto);

		var userCreated = service.create(user);

		return ResponseEntity.status(HttpStatus.CREATED).body(
			new ApiResponse(HttpStatus.CREATED.value(), SUCCESS_MESSAGE, userCreated.getExternalId())
		);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
	public ResponseEntity<ApiResponse> update(@PathVariable UUID id, @RequestBody @Valid UserManDto dto) {
		log.info("Try update user {} with parameters: {}", id, dto);

		var userToUpdate = mapper.toEntity(dto);

		var updatedUser = service.update(id, userToUpdate);

		UserDto userDto = mapper.toDto(updatedUser);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, userDto));
	}

	@GetMapping("/search")
	@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
	public ResponseEntity<ApiResponse> search(
		@RequestParam(name = "document", required = false) String document,
		@RequestParam(name = "name", required = false) String name,
		@RequestParam(name = "email", required = false) String email,
		@RequestParam(name = "role", required = false) String role,
		@ParameterObject @PageableDefault(
			size = 10,
			sort = "createdAt",
			direction = Sort.Direction.DESC
		) Pageable pageable
	) {
		log.info("Search users with filters");

		Page<User> users = service.search(document, name, email, role, pageable);

		var listDto = users.map(mapper::toShortDto);

		return ResponseEntity.ok().body(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, listDto));
	}

	@GetMapping("/available-roles")
	@PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
	public ResponseEntity<ApiResponse> getAvailableRoles() {
		List<RoleOptionDto> roles = Arrays.stream(UserRoleEnum.values())
			.filter(role -> role != UserRoleEnum.ADMIN)
			.map(role -> new RoleOptionDto(role.name(), role.getLabel(), role.getDescription()))
			.toList();

		return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, roles));
	}
}
