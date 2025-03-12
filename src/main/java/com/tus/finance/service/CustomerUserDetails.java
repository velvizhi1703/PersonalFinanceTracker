package com.tus.finance.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.tus.finance.model.Role;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomerUserDetails implements UserDetails {
	  private String email;
	    private String password;
	    private List<GrantedAuthority> authorities; // ✅ Use Authorities Instead of Set<Role>

	    public CustomerUserDetails(String email, String password, List<GrantedAuthority> authorities) {
	        this.email = email;
	        this.password = password;
	        this.authorities = authorities;
	    }

	    @Override
	    public Collection<? extends GrantedAuthority> getAuthorities() {
	        System.out.println("🔹 Extracted Authorities: " + authorities);
	        authorities.forEach(auth -> System.out.println("🔹 Authority: " + auth.getAuthority()));
	        return authorities;
	    }



//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        System.out.println("DEBUG: Authorities for user " + email + ": " + roles);
//        System.out.println("DEBUG: Roles assigned in Spring Security: " + roles);
//
//        if (roles.isEmpty()) {
//            System.out.println("ERROR: No roles found for user " + email);
//        } else {
//            roles.forEach(role -> System.out.println("DEBUG: Mapping Role -> " + role.name()));
//        }
//
//        return roles.stream()
//                .map(role -> new SimpleGrantedAuthority(role.name())) // Ensure Role is an ENUM
//                .collect(Collectors.toList());
//    }


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

//    public Set<Role> getRoles() {
//        return roles;
    }

