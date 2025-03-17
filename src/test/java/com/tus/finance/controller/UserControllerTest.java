package com.tus.finance.controller;

import com.tus.finance.dto.UserAllDTO;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
        testUser.setName("John Doe");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password123");
        testUser.setRoles(Set.of(Role.ROLE_USER));
        testUser.setStatus("Enabled");

        testUserDTO = new UserDTO();
        testUserDTO.setName("John Doe");
        testUserDTO.setEmail("testuser@example.com");
        testUserDTO.setPassword("password123");
        testUserDTO.setRoles(Set.of(Role.ROLE_USER));
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        when(userService.registerUser(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Doe\",\"email\":\"testuser@example.com\",\"password\":\"password123\",\"roles\":[\"ROLE_USER\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"));
    }

    @Test
    void testRegisterUser_UserAlreadyExists() throws Exception {
        when(userService.registerUser(any(User.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Doe\",\"email\":\"testuser@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void testGetCurrentUser_Success() throws Exception {
        when(userService.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@example.com"));
    }

    @Test
    @WithMockUser(username = "nonexistent@example.com", roles = {"USER"})
    void testGetCurrentUser_NotFound() throws Exception {
        when(userService.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isNotFound());
    }

//    @Test
//    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
//    void testGetAllUsers_Success() throws Exception {
//        List<UserAllDTO> users = List.of(new UserAllDTO(testUser));
//        when(userService.getAllUsersWithDetails()).thenReturn(users);
//
//        mockMvc.perform(get("/api/users"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].email").value("testuser@example.com"));
//    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testDeleteAllUsers_Success() throws Exception {
        doNothing().when(userService).deleteAllUsers();

        mockMvc.perform(delete("/api/users/deleteAll"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testToggleUserStatus_Success() throws Exception {
        Map<String, String> requestBody = Map.of("status", "Disabled");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(put("/api/users/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"Disabled\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testToggleUserStatus_UserNotFound() throws Exception {
        Map<String, String> requestBody = Map.of("status", "Disabled");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"Disabled\"}"))
                .andExpect(status().isNotFound());
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
