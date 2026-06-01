package org.project.mechanic_shop.shared.config.security;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.project.mechanic_shop.domain.entities.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

	private final String username;
	private final String password;
	private final Boolean enabled;
	private final Collection<? extends GrantedAuthority> authorities;

	public UserPrincipal(User user) {
		this.username = user.getEmail();
		this.password = user.getPassword();
		this.enabled = user.getActive();
		this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public @Nullable String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return UserDetails.super.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return UserDetails.super.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return UserDetails.super.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}
}
