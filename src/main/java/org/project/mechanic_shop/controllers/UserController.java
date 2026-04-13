package org.project.mechanic_shop.controllers;

import com.auth0.jwt.JWT;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.common.responses.ApiResponse;
import org.project.mechanic_shop.dto.user_dto.UserDto;
import org.project.mechanic_shop.dto.user_dto.UserManDto;
import org.project.mechanic_shop.mappers.UserMapper;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.services.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    public ResponseEntity<ApiResponse> findById(@PathVariable UUID id) {
        log.info("Find user by External ID: {}", id);

        var user = service.findByExternalId(id);
        var dto = mapper.toDto(user);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                SUCCESS_MESSAGE,
                dto
        ));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> create(@RequestBody @Valid UserManDto dto) {
        log.info("Try create user with parameters: {}", dto);

        User user = mapper.toEntity(dto);

        var userCreated = service.create(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(
                HttpStatus.CREATED.value(),
                SUCCESS_MESSAGE,
                userCreated.getExternalId()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(@PathVariable UUID id,
                                              @RequestBody @Valid UserManDto dto, @AuthenticationPrincipal UserDetails userAuth) {
        log.info("Try update user {} with parameters: {}", id, dto);

        var userToUpdate =  mapper.toEntity(dto);

        var updatedUser = service.update(id, userToUpdate, userAuth);

        UserDto userDto = mapper.toDto(updatedUser);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                SUCCESS_MESSAGE,
                userDto
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> search(
            @RequestParam(name = "document", required = false) String document,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "email", required = false) String email,
            @ParameterObject @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Search users with filters");

        Page<User> users = service.search(document, name, email, pageable);

        var listDto = users.map(mapper::toShortDto);

        return ResponseEntity.ok().body(new ApiResponse(
                HttpStatus.OK.value(),
                SUCCESS_MESSAGE,
                listDto
        ));
    }
}