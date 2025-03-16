package com.tus.finance.service;

import com.tus.finance.model.User;
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

    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (user.getRoles().isEmpty()) {
            System.out.println("ERROR: No roles found for user " + email);
        } else {
            user.getRoles().forEach(role -> System.out.println("Found Role -> " + role.name()));
        }
       
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name())) 
                .collect(Collectors.toList());

        System.out.println("User authenticated: " + email);
        System.out.println("Granted Authorities: " + authorities);

        return new CustomerUserDetails(user.getEmail(), user.getPassword(), authorities);
}
}