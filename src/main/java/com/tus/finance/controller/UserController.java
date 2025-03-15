package com.tus.finance.controller;

import com.tus.finance.dto.UserAllDTO;
import com.tus.finance.dto.UserDTO;
import com.tus.finance.exception.UserAlreadyExistsException;
import com.tus.finance.model.User;
import com.tus.finance.repository.UserRepository;
import com.tus.finance.model.Role;
import com.tus.finance.service.UserService;
import org.springframework.security.core.Authentication;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
	private final UserService userService;
	private final UserRepository userRepository;

	public UserController(UserService userService, UserRepository userRepository) {
		this.userService = userService;
		this.userRepository = userRepository;

	}
	
	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@Valid @RequestBody UserDTO userDTO) throws UserAlreadyExistsException {
		User user = new User();
		user.setName(userDTO.getName());
		user.setEmail(userDTO.getEmail());
		user.setPassword(userDTO.getPassword()); 
		
		
		if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
			user.setRoles(Set.of(Role.ROLE_USER)); 
		} else {
			user.setRoles(userDTO.getRoles());
		}

		User savedUser = userService.registerUser(user);
		return ResponseEntity.ok(savedUser);
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	}



	@DeleteMapping("/deleteAll")
	@PreAuthorize("hasRole('ADMIN')") 
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
			email = authentication.getName();  
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


	@PutMapping("/{userId}/status")
	@PreAuthorize("hasRole('ADMIN')") 
	public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId, @RequestBody Map<String, String> requestBody) {
		Optional<User> userOptional = userRepository.findById(userId);

		if (userOptional.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
		}

		User user = userOptional.get();
		String newStatus = requestBody.get("status"); 

		if (!newStatus.equals("Enabled") && !newStatus.equals("Disabled")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid status value.");
		}

		user.setStatus(newStatus); 
		userRepository.save(user);

		return ResponseEntity.ok("User status updated to " + newStatus);
	}

	//    @GetMapping("/user-summary")
	//    public ResponseEntity<List<UserAllDTO>> getUserSummary() {
	//        List<Object[]> results = transactionService.getUserSummary();  // ✅ Use updated method
	//
	//        List<UserAllDTO> response = results.stream()
	//            .map(row -> new UserAllDTO(
	//                (Long) row[0],                      // ✅ Long
	//                (String) row[1],                    // ✅ String
	//                (String) row[2],                    // ✅ String
	//                ((BigDecimal) row[3]).doubleValue(), // ✅ Convert BigDecimal to Double
	//                ((BigDecimal) row[4]).doubleValue(), // ✅ Convert BigDecimal to Double
	//                ((Number) row[5]).intValue(),        // ✅ Convert Number to Integer
	//                (String) row[6]                     // ✅ String
	//            ))
	//            .collect(Collectors.toList());
	//
	//        return ResponseEntity.ok(response);
	//    }



}