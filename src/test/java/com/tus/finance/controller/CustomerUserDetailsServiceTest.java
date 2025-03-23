package com.tus.finance.controller;

import com.tus.finance.model.Role;
import com.tus.finance.model.User;
import com.tus.finance.repository.UserRepository;
import com.tus.finance.service.CustomUserDetailsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("vel@example.com");
        testUser.setPassword("password123");
        testUser.setRoles(Set.of(Role.ROLE_USER));
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByEmail("vel@example.com")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("vel@example.com");

        assertNotNull(userDetails);
        assertEquals("vel@example.com", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("unknown@example.com"));

        assertEquals("User not found: unknown@example.com", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_NoRoles() {
        testUser.setRoles(Set.of()); // User has no roles
        when(userRepository.findByEmail("vel@example.com")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("vel@example.com");

        assertNotNull(userDetails);
        assertEquals("vel@example.com", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty()); // No roles assigned
    }
}
