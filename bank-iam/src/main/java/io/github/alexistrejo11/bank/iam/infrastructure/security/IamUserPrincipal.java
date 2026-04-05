package io.github.alexistrejo11.bank.iam.infrastructure.security;

import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record IamUserPrincipal(UserId userId, String email, Collection<String> permissions) implements UserDetails {

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
