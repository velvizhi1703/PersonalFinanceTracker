package com.tus.studentmanagement.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class DebugSecurityFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            System.out.println("🔍 Debugging Spring Security - Username: " + userDetails.getUsername());
            for (GrantedAuthority authority : userDetails.getAuthorities()) {
                System.out.println("🔹 Granted Authority: " + authority.getAuthority());
            }
        } else {
            System.out.println("❌ No authenticated user found.");
        }

        chain.doFilter(request, response);
    }
}
