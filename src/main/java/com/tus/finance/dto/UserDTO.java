package com.tus.finance.dto;

import java.util.List;

public class UserDTO {
    private String name;
    private String email;
    private String password;
    private List<String> roles; // ✅ Roles are necessary for registration

    public UserDTO() {}

    public UserDTO(String name, String email, String password, List<String> roles) {
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

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
