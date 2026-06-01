package org.project.mechanic_shop.domain.dto.user_dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.enums.UserRoleEnum;

/**
 * DTO for {@link User
 *}
 */
public record UserShortDto(
	UUID externalId,
	LocalDateTime createdAt,
	String createdFor,
	LocalDateTime lastUpdatedAt,
	String lastUpdatedFor,
	UserRoleEnum role,
	String document,
	String name,
	String email,
	Boolean active,
	String phone
) implements Serializable {}
