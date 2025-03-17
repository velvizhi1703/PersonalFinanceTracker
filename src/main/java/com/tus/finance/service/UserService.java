package com.tus.finance.service;

import com.tus.finance.model.User;
import com.tus.finance.dto.UserAllDTO;
import com.tus.finance.exception.UserAlreadyExistsException;
import com.tus.finance.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    public User registerUser(User user) throws UserAlreadyExistsException {
      
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(user.getEmail());
        }
        
    
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

    public List<UserAllDTO> getAllUsersWithDetails() {
        List<User> users = StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .filter(user -> user.getRoles().stream().noneMatch(role -> role.name().equals("ROLE_ADMIN")))
                .collect(Collectors.toList());

        return users.stream().map(user -> new UserAllDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTotalExpense(),
                user.getTotalIncome(),
                user.getNumTransactions(),
                user.getStatus() // 🔹 Return the actual status without forcing "Enabled"
        )).collect(Collectors.toList());
    }

}


