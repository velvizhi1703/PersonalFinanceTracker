package com.tus.finance.config;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.tus.finance.filter.JwtAuthenticationFilter;
import com.tus.finance.service.CustomUserDetailsService;
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final CustomUserDetailsService customUserDetailsService;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(CustomUserDetailsService customUserDetailsService,JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.customUserDetailsService = customUserDetailsService;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		.csrf().disable()
		.authorizeHttpRequests(auth -> auth
				.antMatchers("/api/auth/**").permitAll()
				.antMatchers("/", "/index.html", "/assets/**","/pages/**").permitAll()
				.antMatchers("/api/users/register").permitAll()
				.antMatchers("/api/auth/login").permitAll()
				.antMatchers("/admin_dashboard.html", "/users.html").permitAll()
				.antMatchers("/api/transactions/**").authenticated()
				.antMatchers("/api/dashboard/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
				.antMatchers("/api/**").authenticated()
				.antMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
				.anyRequest().authenticated()
				)
		.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		.authenticationProvider(authenticationProvider())
		.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
		.exceptionHandling()
		.accessDeniedHandler((request, response, accessDeniedException) -> {
			 logger.warn("Access Denied for User: {}", request.getUserPrincipal());
             logger.warn("Required Role: ROLE_ADMIN");
             response.sendError(HttpServletResponse.SC_FORBIDDEN);
         });

		return http.build();
	}
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(customUserDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}
