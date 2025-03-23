package com.tus.finance.config;

import com.tus.finance.filter.JwtAuthenticationFilter;
import com.tus.finance.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @InjectMocks
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(customUserDetailsService, jwtAuthenticationFilter);
    }

    @Test
    void testPasswordEncoder_ShouldReturnBCryptPasswordEncoder() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        assertNotNull(passwordEncoder);
        assertEquals("org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder", passwordEncoder.getClass().getName());
    }

    @Test
    void testAuthenticationProvider_ShouldReturnDaoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = securityConfig.authenticationProvider();
        assertNotNull(authProvider);
       
    }

    @Test
    void testAuthenticationManager_ShouldReturnAuthenticationManager() throws Exception {
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mock(AuthenticationManager.class));

        AuthenticationManager authenticationManager = securityConfig.authenticationManager(authenticationConfiguration);
        assertNotNull(authenticationManager);
    }

}
