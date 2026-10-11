package com.campusgrid;

import com.campusgrid.dto.CreateUserRequest;
import com.campusgrid.dto.UserTableDto;
import com.campusgrid.model.Faculty;
import com.campusgrid.model.Role;
import com.campusgrid.model.Student;
import com.campusgrid.model.User;
import com.campusgrid.repository.FacultyRepository;
import com.campusgrid.repository.StudentRepository;
import com.campusgrid.repository.UserRepository;
import com.campusgrid.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @InjectMocks
    private UserService userService;

    private User primaryAdminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        primaryAdminUser = new User();
        primaryAdminUser.setId(1L);
        primaryAdminUser.setUsername("admin");
        primaryAdminUser.setEmail("admin@campusgrid.edu");
        primaryAdminUser.setFullName("System Administrator");
        primaryAdminUser.setRole(Role.ADMIN);
        primaryAdminUser.setActive(true);

        normalUser = new User();
        normalUser.setId(5L);
        normalUser.setUsername("alice123");
        normalUser.setEmail("alice@campusgrid.edu");
        normalUser.setFullName("Alice Smith");
        normalUser.setRole(Role.STUDENT);
        normalUser.setActive(true);
    }

    @Test
    @DisplayName("Should block deactivation of primary admin by username")
    void testBlockDeactivationOfPrimaryAdminByUsername() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(primaryAdminUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            userService.toggleUserStatus(1L);
        });
        assertTrue(ex.getMessage().contains("primary administrator account cannot be deactivated"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should block deactivation of primary admin by ID 1 even if username is different")
    void testBlockDeactivationOfAdminById() {
        User rootAdmin = new User();
        rootAdmin.setId(1L);
        rootAdmin.setUsername("superadmin");
        rootAdmin.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(rootAdmin));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            userService.toggleUserStatus(1L);
        });
        assertTrue(ex.getMessage().contains("primary administrator account cannot be deactivated"));
    }

    @Test
    @DisplayName("Should allow toggling status of normal users")
    void testAllowTogglingNormalUserStatus() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(normalUser));
        when(userRepository.save(any(User.class))).thenReturn(normalUser);

        boolean newStatus = userService.toggleUserStatus(5L);
        assertFalse(newStatus); // Toggled from true to false
        verify(userRepository, times(1)).save(normalUser);
    }

    @Test
    @DisplayName("Should block deletion of primary admin account")
    void testBlockDeletionOfPrimaryAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(primaryAdminUser));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            userService.deleteUser(1L);
        });
        assertTrue(ex.getMessage().contains("primary administrator account cannot be deleted"));
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete student associated records before deleting parent user")
    void testDeleteStudentCascade() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(normalUser));
        Student mockStudent = new Student();
        when(studentRepository.findByUserId(5L)).thenReturn(Optional.of(mockStudent));

        userService.deleteUser(5L);

        verify(studentRepository, times(1)).delete(mockStudent);
        verify(userRepository, times(1)).delete(normalUser);
    }

    @Test
    @DisplayName("Should reject student creation when username is already taken")
    void testRejectDuplicateUsername() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("admin");
        req.setEmail("new@campusgrid.edu");
        req.setPassword("Password@123");
        req.setFullName("New User");
        req.setRole(Role.STUDENT);

        when(userRepository.existsByUsername("admin")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(req);
        });
        assertTrue(ex.getMessage().contains("already taken"));
    }

    @Test
    @DisplayName("Should reject password reset when password is too short")
    void testRejectShortPasswordOnReset() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.resetPassword(5L, "123");
        });
        assertTrue(ex.getMessage().contains("at least 6 characters"));
    }
}
