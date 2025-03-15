package com.tus.finance.dto;

import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import com.tus.finance.model.Role;

public class UserDTO {
	@NotEmpty(message = "Name must be provided.")
    private String name;
	@Email(message = "Email must be valid.")
    private String email;
	@NotEmpty(message = "Password must be provided")
    private String password;
    private Set<Role> roles; // ✅ Roles are necessary for registration

    public UserDTO() {}

    public UserDTO(String name, String email, String password, Set<Role> roles) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
