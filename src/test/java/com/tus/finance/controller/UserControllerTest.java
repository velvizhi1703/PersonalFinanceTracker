package com.tus.finance.controller;

import com.tus.finance.dto.UserDTO;
import com.tus.finance.exception.UserAlreadyExistsException;
import com.tus.finance.model.Role;
import com.tus.finance.model.User;
import com.tus.finance.repository.UserRepository;
import com.tus.finance.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
     
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Velvizhi");
        testUser.setEmail("vel@example.com");
        testUser.setPassword("password123");
        testUser.setRoles(Set.of(Role.ROLE_USER));
        testUser.setStatus("Enabled");

        testUserDTO = new UserDTO();
        testUserDTO.setName("Velvizhi");
        testUserDTO.setEmail("vel@example.com");
        testUserDTO.setPassword("password123");
        testUserDTO.setRoles(Set.of(Role.ROLE_USER));
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        when(userService.registerUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Velvizhi\",\"email\":\"vel@example.com\",\"password\":\"password123\",\"roles\":[\"ROLE_USER\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Velvizhi"))
                .andExpect(jsonPath("$.email").value("vel@example.com"));
    }

    @Test
    void testRegisterUser_UserAlreadyExists() throws Exception {
        when(userService.registerUser(any(User.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Velvizhi\",\"email\":\"vel@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict());
    }


    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testToggleUserStatus_InvalidStatus() throws Exception {
        Map<String, String> requestBody = Map.of("status", "UnknownStatus");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/api/users/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"UnknownStatus\"}"))
                .andExpect(status().isBadRequest());
    }
}
