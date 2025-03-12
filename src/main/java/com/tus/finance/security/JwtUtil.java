package com.tus.finance.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.tus.finance.model.Role;
import com.tus.finance.model.User;

import java.security.Key;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component  // ✅ Ensures Spring Boot detects this class as a Bean
public class JwtUtil {
   // private static final String SECRET_KEY = "mysupersecurejwtsecretkeywith256bits"; 
	private static final String SECRET_KEY = "n8vR+zTMEkKf/D5Oa6t5qE3nKMtJhGTPM5p+5Aowxgk=";

// ✅ At least 32 characters

//    private Key getSigningKey() {
//        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
//    }
	private Key getSigningKey() {
	    return Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY));
	}


	public String generateToken(String email, Collection<? extends GrantedAuthority> authorities) {
	    List<String> roles = authorities.stream()
	        .map(GrantedAuthority::getAuthority)
	        .collect(Collectors.toList());

	    System.out.println("✅ Generating JWT for: " + email);
	    System.out.println("✅ Roles in JWT: " + roles);

	    return Jwts.builder()

	    		.setSubject(email)
	        .claim("roles", roles)
	        .setIssuedAt(new Date())
	        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour expiry
	        .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // ✅ FIX: Use getSigningKey()
            .compact();

	}
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();
        return expiration.before(new Date());
    }
    public List<String> extractRoles(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        List<String> roles = claims.get("roles", List.class);
        System.out.println("✅ Extracted Roles from JWT: " + roles); // Debug log
        return roles;
    }

    public List<GrantedAuthority> extractAuthorities(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        List<String> roles = claims.get("roles", List.class);
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }


}