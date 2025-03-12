package com.tus.finance.controller;

import com.tus.finance.dto.UserAllDTO;
import com.tus.finance.dto.UserDTO;
import com.tus.finance.model.User;
import com.tus.finance.repository.UserRepository;
import com.tus.finance.model.Role;
import com.tus.finance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
        System.out.println("DEBUG: Incoming registration request: Name = " + userDTO.getName() + ", Email = " + userDTO.getEmail());
        System.out.println("DEBUG: Password received = '" + userDTO.getPassword() + "'");
        System.out.println("DEBUG: Roles received = " + userDTO.getRoles());

        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            System.out.println("ERROR: Password received as null or empty!");
            return ResponseEntity.badRequest().body("Password cannot be null or empty");
        }

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword()); // Will be encoded in UserService

        // ✅ Assign roles properly, ensuring they are prefixed correctly
        if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
            user.setRoles(Set.of(Role.ROLE_USER)); // Default role: ROLE_USER
        } else {
            Set<Role> assignedRoles = userDTO.getRoles().stream()
                    .map(role -> {
                        try {
                            // ✅ Ensure roles are in the correct format (e.g., ROLE_ADMIN, ROLE_USER)
                            if (!role.startsWith("ROLE_")) {
                                role = "ROLE_" + role;
                            }
                            return Role.valueOf(role); // ✅ Match with Enum values
                        } catch (IllegalArgumentException e) {
                            System.out.println("WARNING: Invalid role received: " + role);
                            return Role.ROLE_USER; // Default if invalid role provided
                        }
                    })
                    .collect(Collectors.toSet());

            user.setRoles(assignedRoles);
        }

        User savedUser = userService.registerUser(user);
        return ResponseEntity.ok(savedUser);
    }
    
    @DeleteMapping("/deleteAll")
    @PreAuthorize("hasRole('ADMIN')") // Only admins can delete all users
    public ResponseEntity<String> deleteAllUsers() {
        userService.deleteAllUsers();
        return ResponseEntity.ok("All users have been deleted.");
    }
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        String email;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            email = authentication.getName();  // Fallback for JWT tokens
        }

        Optional<User> user = userService.findByEmail(email);
        return user.map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserAllDTO>> getAllUsers() {
        List<UserAllDTO> users = userService.getAllUsersWithDetails();
        return ResponseEntity.ok(users);
    }



}