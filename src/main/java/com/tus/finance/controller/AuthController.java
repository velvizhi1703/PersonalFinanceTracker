package com.tus.finance.controller;

import com.tus.finance.security.JwtUtil;
import com.tus.finance.dto.LoginRequest;
import com.tus.finance.model.Role;
import com.tus.finance.model.User;
import com.tus.finance.repository.UserRepository;
import com.tus.finance.service.CustomUserDetailsService;
import com.tus.finance.service.CustomerUserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    public AuthController(JwtUtil jwtUtil, AuthenticationManager authenticationManager,UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("DEBUG: Login attempt - " + request.getEmail());

            // ✅ Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            System.out.println("DEBUG: Authentication success for " + request.getEmail());

            // ✅ Fetch user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // ✅ Extract roles (assuming a single role per user)
            String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER"); // Default role if none found
      

            String token = jwtUtil.generateToken(request.getEmail(), authentication.getAuthorities());

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return ResponseEntity.ok(Map.of(
                "userId", user.getId(),  // ✅ Include userId
                "token", token,
                "role", role,
                "redirectUrl", role.equals("ROLE_ADMIN") ? "/admin_dashboard.html" : "/user_dashboard.html"
            ));

        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
        }
    }
}