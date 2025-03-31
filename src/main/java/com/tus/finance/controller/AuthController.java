package com.tus.finance.controller;

import com.tus.finance.security.JwtUtil;
import com.tus.finance.dto.LoginRequest;
import com.tus.finance.dto.LoginResponseDto;
import com.tus.finance.model.User;
import com.tus.finance.repository.UserRepository;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
	private final JwtUtil jwtUtil;
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;

	public AuthController(JwtUtil jwtUtil, AuthenticationManager authenticationManager,UserRepository userRepository) {
		this.jwtUtil = jwtUtil;
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
	}
	@PostMapping("/login")
	public ResponseEntity<EntityModel<LoginResponseDto>> login(@RequestBody LoginRequest request) {
		try {
			logger.debug("Login attempt - {}", request.getEmail());
			User user = userRepository.findByEmail(request.getEmail())
					.orElseThrow(() -> new UsernameNotFoundException("User not found"));

			  if ("Disabled".equalsIgnoreCase(user.getStatus())) {
	                logger.warn("Login attempt for disabled user: {}", request.getEmail());
	                return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                        .body(EntityModel.of(new LoginResponseDto(), 
	                                linkTo(methodOn(AuthController.class).login(request)).withSelfRel()
	                                ));
	            }

			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
					);
			  logger.debug("Authentication success for {}", request.getEmail());

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
			EntityModel<LoginResponseDto> entityModel = EntityModel.of(loginResponse,
					linkTo(methodOn(AuthController.class).login(request)).withSelfRel(),
					linkTo(methodOn(TransactionController.class).getUserTransactions(null)).withRel("user-transactions"),
					linkTo(methodOn(TransactionController.class).getDashboardStats("Bearer " + token)).withRel("user-dashboard")
					);

			return ResponseEntity.ok(entityModel);
		} catch (Exception e) {
			logger.error("Error during login: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(EntityModel.of(new LoginResponseDto(), 
							linkTo(methodOn(AuthController.class).login(request)).withSelfRel()
							));
		}
	}
}