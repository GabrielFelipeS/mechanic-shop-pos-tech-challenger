package org.project.mechanic_shop.application.mappers;

import org.mapstruct.Mapper;
import org.project.mechanic_shop.domain.dto.user_dto.UserDto;
import org.project.mechanic_shop.domain.dto.user_dto.UserManDto;
import org.project.mechanic_shop.domain.dto.user_dto.UserShortDto;
import org.project.mechanic_shop.domain.entities.user.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
	User toEntity(UserManDto customerManDto);

	UserShortDto toShortDto(User customer);

	UserDto toDto(User customer);
}
