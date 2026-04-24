package org.project.mechanic_shop.utils;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

public class AuthUtil {
    public static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor admin() {
        return user("integration-admin").roles("ADMIN");
    }
}
