package com.tus.finance.controller;

import com.tus.finance.security.JwtUtil;
import com.tus.finance.dto.LoginRequest;
import com.tus.finance.dto.LoginResponseDto;
import com.tus.finance.model.User;
import com.tus.finance.repository.UserRepository;

import java.util.Map;

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
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

           
            if ("Disabled".equalsIgnoreCase(user.getStatus())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", " Your account is disabled. Contact Admin!"));
            }
          
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            System.out.println("DEBUG: Authentication success for " + request.getEmail());

        
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

         
            String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER"); 
      

            String token = jwtUtil.generateToken(request.getEmail(), authentication.getAuthorities());

            
            LoginResponseDto loginResponse = new LoginResponseDto();
            loginResponse.setUserId(user.getId());
            loginResponse.setRole(role);
            loginResponse.setToken(token);

            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
        }
    }
}