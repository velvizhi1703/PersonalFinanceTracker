package com.tus.finance.service;

import org.springframework.security.core.GrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public class CustomerUserDetails implements UserDetails {
	 private static final Logger logger = LoggerFactory.getLogger(CustomerUserDetails.class);
	private String email;
	private String password;
	private List<GrantedAuthority> authorities;

	public CustomerUserDetails(String email, String password, List<GrantedAuthority> authorities) {
		this.email = email;
		this.password = password;
		this.authorities = authorities;
	}

	@Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        logger.debug("Extracted Authorities: {}", authorities);
        authorities.forEach(auth -> logger.debug("Authority: {}", auth.getAuthority()));
        return authorities;
    }

	@Override
	public String getPassword() { return password; }

	@Override
	public String getUsername() { return email; }

	@Override
	public boolean isAccountNonExpired() { return true; }

	@Override
	public boolean isAccountNonLocked() { return true; }

	@Override
	public boolean isCredentialsNonExpired() { return true; }

	@Override
	public boolean isEnabled() { return true; }
}

