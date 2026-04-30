package org.project.mechanic_shop.config.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.models.User;
import org.project.mechanic_shop.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemBootstrapper {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.seed.password}")
	private String seedPassword;

	@Value("${app.seed.warehouse.name}")
	private String whName;

	@Value("${app.seed.warehouse.email}")
	private String whEmail;

	@Value("${app.seed.warehouse.document}")
	private String whDoc;

	@Value("${app.seed.warehouse.role}")
	private String whRole;

	@Value("${app.seed.receptionist.name}")
	private String rcName;

	@Value("${app.seed.receptionist.email}")
	private String rcEmail;

	@Value("${app.seed.receptionist.document}")
	private String rcDoc;

	@Value("${app.seed.receptionist.role}")
	private String rcRole;

	@Value("${app.seed.mechanic.name}")
	private String mcName;

	@Value("${app.seed.mechanic.email}")
	private String mcEmail;

	@Value("${app.seed.mechanic.document}")
	private String mcDoc;

	@Value("${app.seed.mechanic.role}")
	private String mcRole;

	@Value("${app.seed.admin.name}")
	private String adName;

	@Value("${app.seed.admin.email}")
	private String adEmail;

	@Value("${app.seed.admin.document}")
	private String adDoc;

	@Value("${app.seed.admin.role}")
	private String adRole;

	@Value("${app.seed.buyer.name}")
	private String byName;

	@Value("${app.seed.buyer.email}")
	private String byEmail;

	@Value("${app.seed.buyer.document}")
	private String byDoc;

	@Value("${app.seed.buyer.role}")
	private String byRole;

	@EventListener(ApplicationReadyEvent.class)
	@Transactional
	public void onApplicationReady() {
		log.info("Verifying system initialization state...");

		String[][] defaultUsers = {
			{ adRole, adEmail, adDoc, adName },
			{ whRole, whEmail, whDoc, whName },
			{ rcRole, rcEmail, rcDoc, rcName },
			{ mcRole, mcEmail, mcDoc, mcName },
			{ byRole, byEmail, byDoc, byName },
		};

		boolean usersCreated = false;

		for (String[] userData : defaultUsers) {
			String role = userData[0];
			String email = userData[1];
			String document = userData[2];
			String name = userData[3];

			if (!userRepository.existsByRole(role)) {
				log.info("Role '{}' not found. Creating default user...", role);
				createDefaultUser(role, email, document, name);
				usersCreated = true;
			} else {
				log.info("Role '{}' already exists. Skipping creation.", role);
			}
		}

		if (usersCreated) {
			log.info(
				"\n##############################################################\nSystem Bootstrap Completed: Default Users Created!"
			);
		} else {
			log.info("System Bootstrap Completed: No new users were needed.");
		}
	}

	private void createDefaultUser(String role, String email, String document, String name) {
		User defaultUser = new User();

		defaultUser.setName(name);
		defaultUser.setEmail(email);
		defaultUser.setPassword(passwordEncoder.encode(seedPassword));
		defaultUser.setRole(role);
		defaultUser.setDocument(document);
		defaultUser.setActive(true);
		defaultUser.setPhone("00000000000");

		User savedUser = userRepository.save(defaultUser);

		log.info(
			"Created DEFAULT USER '{}' with Role '{}' on APPLICATION (ID: {})",
			savedUser.getEmail(),
			savedUser.getRole(),
			savedUser.getId()
		);
	}
}
