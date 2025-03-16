package com.tus.finance.controller;

import com.tus.finance.dto.UserAllDTO;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import com.tus.finance.dto.UserDTO;
import com.tus.finance.exception.UserAlreadyExistsException;
import com.tus.finance.model.User;
import com.tus.finance.repository.UserRepository;
import com.tus.finance.model.Role;
import com.tus.finance.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.EntityModel;
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
		UserDTO responseDto = new UserDTO(savedUser); 
		responseDto.add(linkTo(methodOn(UserController.class).registerUser(userDTO)).withSelfRel());
		responseDto.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));
		responseDto.add(linkTo(methodOn(UserController.class).getCurrentUser(null)).withRel("current-user"));
		System.out.println("HATEOAS Links: " + responseDto.getLinks());
		return ResponseEntity.ok(responseDto);
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	}
	@DeleteMapping("/deleteAll")
	@PreAuthorize("hasRole('ADMIN')") 
	public ResponseEntity<EntityModel<String>> deleteAllUsers() {
		userService.deleteAllUsers();
		EntityModel<String> response = EntityModel.of(
				"All users have been deleted.",
				linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users")
				);

		return ResponseEntity.ok(response);
	}
	@GetMapping("/me")
	@PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
		return userService.findByEmail(userDetails.getUsername())
				.map(user -> {
					UserDTO userDTO = new UserDTO(user);
					userDTO.add(linkTo(methodOn(UserController.class).getCurrentUser(userDetails)).withSelfRel());
					userDTO.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));
					return ResponseEntity.ok(userDTO);
				})
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<UserAllDTO>> getAllUsers() {
		List<UserAllDTO> users = userService.getAllUsersWithDetails();

		users.forEach(user -> {
			user.add(linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
			user.add(linkTo(methodOn(UserController.class).getCurrentUser(null)).withRel("current-user"));
		});

		return ResponseEntity.ok(users);
	}

	@PutMapping("/{userId}/status")
	@PreAuthorize("hasRole('ADMIN')") 
	public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId, @RequestBody Map<String, String> requestBody) {
		Optional<User> userOptional = userRepository.findById(userId);
		if (userOptional.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(EntityModel.of(Map.of("message", "User not found.")));
		}

		User user = userOptional.get();
		String newStatus = requestBody.get("status");

		if (!newStatus.equals("Enabled") && !newStatus.equals("Disabled")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(EntityModel.of(Map.of("message", "Invalid status value.")));
		}

		user.setStatus(newStatus);
		userRepository.save(user);
		Map<String, String> responseBody = Map.of("message", "User status updated to " + newStatus);

		EntityModel<Map<String, String>> response = EntityModel.of(
				responseBody,
				linkTo(methodOn(UserController.class).toggleUserStatus(userId, requestBody)).withSelfRel(),
				linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"),
				linkTo(methodOn(UserController.class).getCurrentUser(null)).withRel("current-user")
				);

		return ResponseEntity.ok(response);
	}
}