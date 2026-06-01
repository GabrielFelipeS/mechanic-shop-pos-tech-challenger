package org.project.mechanic_shop.utils;

import java.util.Set;
import org.project.mechanic_shop.domain.dto.user_dto.UserManDto;
import org.project.mechanic_shop.domain.entities.user.User;
import org.project.mechanic_shop.domain.enums.UserRoleEnum;

public class UserHelper {

	public static User generateUser() {
		return new User(
			1L,
			"57096255079",
			"TESTE",
			"teste@test.com",
			"ADMIN",
			true,
			"Teste@123",
			"553470167400",
			Set.of()
		);
	}

	public static UserManDto generateUserMenDto() {
		return generateUserMenDto(generateUser());
	}

	public static UserManDto generateUserMenDto(User user) {
		return new UserManDto(
			user.getDocument(),
			user.getName(),
			user.getEmail(),
			UserRoleEnum.valueOf(user.getRole()),
			user.getActive(),
			user.getPassword(),
			user.getPhone()
		);
	}
}
