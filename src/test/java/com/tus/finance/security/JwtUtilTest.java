package com.tus.finance.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.lang.reflect.Method;
import java.security.Key;
@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private UserDetails userDetails;

    private String generatedToken;
    private String email = "vel@example.com";
    private List<GrantedAuthority> authorities;

    @BeforeEach
    void setUp() {
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        generatedToken = jwtUtil.generateToken(email, authorities);
    }

    @Test
    void testGenerateToken_Success() {
        assertNotNull(generatedToken);
        assertFalse(generatedToken.isEmpty());
    }

    @Test
    void testExtractUsername_Success() {
        String extractedUsername = jwtUtil.extractUsername(generatedToken);
        assertEquals(email, extractedUsername);
    }

    @Test
    void testValidateToken_ValidToken() {
        when(userDetails.getUsername()).thenReturn(email);

        boolean isValid = jwtUtil.validateToken(generatedToken, userDetails);

        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        when(userDetails.getUsername()).thenReturn("wrong@example.com");

        boolean isValid = jwtUtil.validateToken(generatedToken, userDetails);

        assertFalse(isValid);
    }

//    @Test
//    void testValidateToken_ExpiredToken() throws Exception {
//        Method method = JwtUtil.class.getDeclaredMethod("getSigningKey");
//        method.setAccessible(true);
//        Key key = (Key) method.invoke(jwtUtil);
//
//        String expiredToken = Jwts.builder()
//                .setSubject(email)
//                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60))
//                .setExpiration(new Date(System.currentTimeMillis() - 1000))
//                .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS256)
//                .compact();
//
//        when(userDetails.getUsername()).thenReturn(email);
//
//        boolean isValid = jwtUtil.validateToken(expiredToken, userDetails);
//
//       // assertFalse(isValid);
//    }
    @Test
    void testExtractRoles_Success() {
        List<String> extractedRoles = jwtUtil.extractRoles(generatedToken);

        assertNotNull(extractedRoles);
        assertEquals(1, extractedRoles.size());
        assertEquals("ROLE_USER", extractedRoles.get(0));
    }

    @Test
    void testExtractAuthorities_Success() {
        List<GrantedAuthority> extractedAuthorities = jwtUtil.extractAuthorities(generatedToken);

        assertNotNull(extractedAuthorities);
        assertEquals(1, extractedAuthorities.size());
        assertEquals("ROLE_USER", extractedAuthorities.get(0).getAuthority());
    }
}
