package org.project.mechanic_shop.utils;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

public class AuthUtil {

	public static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor admin() {
		return user("integration-admin").roles("ADMIN");
	}
}
