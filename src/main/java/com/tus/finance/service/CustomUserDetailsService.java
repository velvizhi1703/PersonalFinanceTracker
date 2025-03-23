package com.tus.finance.service;

import com.tus.finance.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tus.finance.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	 private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (user.getRoles().isEmpty()) {
            logger.error("No roles found for user: {}", email);
        } else {
            user.getRoles().forEach(role -> logger.debug("Found Role -> {}", role.name()));
        }

        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());

        logger.info("User authenticated: {}", email); 
        logger.debug("Granted Authorities: {}", authorities); 

        return new CustomerUserDetails(user.getEmail(), user.getPassword(), authorities);
    }
}