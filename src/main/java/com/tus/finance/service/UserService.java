package com.tus.finance.service;

import com.tus.finance.model.User;
import com.tus.finance.dto.UserAllDTO;
import com.tus.finance.dto.UserDTO;
import com.tus.finance.model.Role;
import com.tus.finance.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User registerUser(User user) {
        System.out.println("Received user - Name: " + user.getName() + ", Email: " + user.getEmail());

        // ✅ Check if the email is already registered
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use!");
        }

        // ✅ Check if password is provided
        if (!StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // ✅ Hash the password before saving to the database
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // ✅ Assign roles based on request, ensuring correct enum formatting
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Collections.singleton(Role.ROLE_USER));  // ✅ Default role is now ROLE_USER
        } else {
            Set<Role> assignedRoles = user.getRoles().stream()
                .map(role -> {
                    try {
                        // ✅ Ensure the role is in the correct format (e.g., ROLE_ADMIN)
                        return Role.valueOf(role.name());
                    } catch (IllegalArgumentException e) {
                        System.out.println("WARNING: Invalid role received: " + role);
                        return Role.ROLE_USER; // Default if invalid role provided
                    }
                })
                .collect(Collectors.toSet());

            user.setRoles(assignedRoles);
        }

        return userRepository.save(user);
    }
    
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    public List<UserAllDTO> getAllUsersWithDetails() {
        List<Object[]> rawData = userRepository.getRawUserData(); // Fetch raw query results

        // Debug: Print raw query data
        for (Object[] row : rawData) {
            System.out.println(Arrays.toString(row));  // Debugging output
        }

        return rawData.stream().map(row ->
            new UserAllDTO(
                (row[0] != null) ? ((Number) row[0]).longValue() : 0L,  // ID -> Long
                (String) row[1],  // Name -> String
                (String) row[2],  // Email -> String
                (row[3] != null) ? ((Number) row[3]).doubleValue() : 0.0,  // Total Expenses -> Double
                (row[4] != null) ? ((Number) row[4]).doubleValue() : 0.0,  // Total Income -> Double
                (row[5] != null) ? ((Number) row[5]).longValue() : 0L,  // Transaction Count -> Long
                (String) row[6]  // Status -> String
            )
        ).collect(Collectors.toList());
    }




}


