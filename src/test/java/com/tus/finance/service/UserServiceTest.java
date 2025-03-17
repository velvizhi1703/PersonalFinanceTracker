package com.tus.finance.service;

import com.tus.finance.dto.UserAllDTO;
import com.tus.finance.exception.UserAlreadyExistsException;
import com.tus.finance.model.Role;
import com.tus.finance.model.User;
import com.tus.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Velvizhi");
        testUser.setEmail("vel@example.com");
        testUser.setPassword("password123");
        testUser.setRoles(Set.of(Role.ROLE_USER));
        testUser.setTotalIncome(5000.0);
        testUser.setTotalExpense(2000.0);
        testUser.setNumTransactions(10);
        testUser.setStatus("Enabled");
    }

    @Test
    void testFindByEmail_Success() {
        when(userRepository.findByEmail("vel@example.com")).thenReturn(Optional.of(testUser));

        Optional<User> foundUser = userService.findByEmail("vel@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("vel@example.com", foundUser.get().getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.findByEmail("unknown@example.com");

        assertFalse(foundUser.isPresent());
    }

    @Test
    void testRegisterUser_Success() throws UserAlreadyExistsException {
        when(userRepository.findByEmail("vel@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User registeredUser = userService.registerUser(testUser);

        assertNotNull(registeredUser);
        assertEquals("vel@example.com", registeredUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

//    @Test
//    void testRegisterUser_UserAlreadyExists() {
//        when(userRepository.findByEmail("vel@example.com")).thenReturn(Optional.of(testUser));
//
//        Exception exception = assertThrows(UserAlreadyExistsException.class, () ->
//                //userService.registerUser(testUser));
//
//        //assertEquals("vel@example.com", exception.getMessage());
//    }

    @Test
    void testDeleteAllUsers_Success() {
        doNothing().when(userRepository).deleteAll();

        assertDoesNotThrow(() -> userService.deleteAllUsers());

        verify(userRepository, times(1)).deleteAll();
    }

    @Test
    void testGetAllUsersWithDetails_Success() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRoles(Set.of(Role.ROLE_ADMIN));

        when(userRepository.findAll()).thenReturn(List.of(testUser, adminUser));

        List<UserAllDTO> usersList = userService.getAllUsersWithDetails();

        assertNotNull(usersList);
        assertEquals(1, usersList.size());
        assertEquals("vel@example.com", usersList.get(0).getEmail());
    }
}
