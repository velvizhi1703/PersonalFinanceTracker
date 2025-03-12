package com.tus.finance.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
	 ROLE_ADMIN,  // ✅ Now matches database values
	  ROLE_USER;

    @Override
    public String getAuthority() {
        return name();
    }
}
