package com.company.gym.service;

import com.company.gym.dao.UserDAO;
import com.company.gym.entity.User;
import com.company.gym.exception.ValidationException;
import com.company.gym.util.PasswordUtil;
import com.company.gym.util.UserCredentialGenerator;
import com.company.gym.util.UsernameUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserDAO userDAO;

    private MeterRegistry meterRegistry;

    private AuthService authService;

    private MockedStatic<PasswordUtil> passwordUtilMock;
    private MockedStatic<UsernameUtil> usernameUtilMock;
    private MockedStatic<UserCredentialGenerator> userCredentialGeneratorMock;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();

        authService = new AuthService(userDAO, meterRegistry);

        passwordUtilMock = mockStatic(PasswordUtil.class);
        usernameUtilMock = mockStatic(UsernameUtil.class);
        userCredentialGeneratorMock = mockStatic(UserCredentialGenerator.class);
    }

    @AfterEach
    void tearDown() {
        passwordUtilMock.close();
        usernameUtilMock.close();
        userCredentialGeneratorMock.close();
    }

    @Test
    void testAssignUniqueUsernameAndPassword_NewUser() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        String baseUsername = "jdoe";
        String plainPassword = "plainPassword123";
        String hashedPassword = "hashedPassword123";

        usernameUtilMock.when(() -> UsernameUtil.generateBaseUsername("John", "Doe")).thenReturn(baseUsername);
        userCredentialGeneratorMock.when(UserCredentialGenerator::generatePassword).thenReturn(plainPassword);
        passwordUtilMock.when(() -> PasswordUtil.hashPassword(plainPassword)).thenReturn(hashedPassword);

        when(userDAO.findByUsername(baseUsername)).thenReturn(null);

        String resultPassword = authService.assignUniqueUsernameAndPassword(user);

        assertEquals(plainPassword, resultPassword);
        assertEquals(baseUsername, user.getUsername());
        assertEquals(hashedPassword, user.getPassword());
        assertTrue(user.getIsActive());

        assertEquals(1.0, meterRegistry.get("app.user.registration.total").counter().count());
    }

    @Test
    void testAssignUniqueUsernameAndPassword_UsernameCollision() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        String baseUsername = "jdoe";
        String uniqueUsername = "jdoe1";
        String plainPassword = "plainPassword123";
        String hashedPassword = "hashedPassword123";

        usernameUtilMock.when(() -> UsernameUtil.generateBaseUsername("John", "Doe")).thenReturn(baseUsername);
        userCredentialGeneratorMock.when(UserCredentialGenerator::generatePassword).thenReturn(plainPassword);
        passwordUtilMock.when(() -> PasswordUtil.hashPassword(plainPassword)).thenReturn(hashedPassword);

        when(userDAO.findByUsername(baseUsername)).thenReturn(new User());
        when(userDAO.findByUsername(uniqueUsername)).thenReturn(null);

        String resultPassword = authService.assignUniqueUsernameAndPassword(user);

        assertEquals(plainPassword, resultPassword);
        assertEquals(uniqueUsername, user.getUsername());
        assertEquals(hashedPassword, user.getPassword());
        assertTrue(user.getIsActive());

        verify(userDAO).findByUsername(baseUsername);
        verify(userDAO).findByUsername(uniqueUsername);

        assertEquals(1.0, meterRegistry.get("app.user.registration.total").counter().count());
    }

    @Test
    void testAssignUniqueUsernameAndPassword_UsernameGenerationFails() {
        User user = new User();
        user.setFirstName("Invalid-");
        user.setLastName("User-");

        usernameUtilMock.when(() -> UsernameUtil.generateBaseUsername("Invalid-", "User-"))
                .thenThrow(new IllegalArgumentException("Invalid characters"));

        userCredentialGeneratorMock.when(UserCredentialGenerator::generatePassword).thenReturn("pass");
        passwordUtilMock.when(() -> PasswordUtil.hashPassword("pass")).thenReturn("hash");

        when(userDAO.findByUsername("")).thenReturn(null);

        String resultPassword = authService.assignUniqueUsernameAndPassword(user);

        assertEquals("pass", resultPassword);
        assertEquals("", user.getUsername());
        assertEquals("hash", user.getPassword());
        assertTrue(user.getIsActive());
        assertEquals(1.0, meterRegistry.get("app.user.registration.total").counter().count());
    }

    @Test
    void testIsUsernameTaken_True() {
        when(userDAO.findByUsername("takenUser")).thenReturn(new User());

        assertTrue(authService.isUsernameTaken("takenUser"));
    }

    @Test
    void testIsUsernameTaken_False() {
        when(userDAO.findByUsername("freeUser")).thenReturn(null);

        assertFalse(authService.isUsernameTaken("freeUser"));
    }

    @Test
    void testAuthenticateUser_Success() {
        User user = new User();
        user.setIsActive(true);
        user.setPassword("hashedPassword");

        when(userDAO.findByUsername("user")).thenReturn(user);
        passwordUtilMock.when(() -> PasswordUtil.checkPassword("plainPassword", "hashedPassword")).thenReturn(true);

        assertTrue(authService.authenticateUser("user", "plainPassword"));
    }

    @Test
    void testAuthenticateUser_UserNotFound() {
        when(userDAO.findByUsername("unknownUser")).thenReturn(null);

        assertFalse(authService.authenticateUser("unknownUser", "password"));
    }

    @Test
    void testAuthenticateUser_UserDeactivated() {
        User user = new User();
        user.setIsActive(false);
        user.setPassword("hashedPassword");

        when(userDAO.findByUsername("inactiveUser")).thenReturn(user);

        assertFalse(authService.authenticateUser("inactiveUser", "password"));
        passwordUtilMock.verifyNoInteractions();
    }

    @Test
    void testAuthenticateUser_InvalidPassword() {
        User user = new User();
        user.setIsActive(true);
        user.setPassword("hashedPassword");

        when(userDAO.findByUsername("user")).thenReturn(user);
        passwordUtilMock.when(() -> PasswordUtil.checkPassword("wrongPassword", "hashedPassword")).thenReturn(false);

        assertFalse(authService.authenticateUser("user", "wrongPassword"));
    }

    @Test
    void testChangePassword_Success() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("oldHashed");

        String oldPlain = "oldPassword";
        String newPlain = "newPassword";
        String newHashed = "newHashed";

        when(userDAO.findByUsername("user")).thenReturn(user);
        passwordUtilMock.when(() -> PasswordUtil.checkPassword(oldPlain, "oldHashed")).thenReturn(true);
        passwordUtilMock.when(() -> PasswordUtil.hashPassword(newPlain)).thenReturn(newHashed);

        assertDoesNotThrow(() -> authService.changePassword("user", oldPlain, newPlain));

        assertEquals(newHashed, user.getPassword());
        verify(userDAO).update(user);
    }

    @Test
    void testChangePassword_UserNotFound() {
        when(userDAO.findByUsername("unknownUser")).thenReturn(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.changePassword("unknownUser", "old", "new");
        });

        assertEquals("User profile not found for password change: unknownUser", exception.getMessage());
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void testChangePassword_IncorrectOldPassword() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("oldHashed");

        String wrongOldPlain = "wrongOldPassword";

        when(userDAO.findByUsername("user")).thenReturn(user);
        passwordUtilMock.when(() -> PasswordUtil.checkPassword(wrongOldPlain, "oldHashed")).thenReturn(false);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.changePassword("user", wrongOldPlain, "new");
        });

        assertEquals("Incorrect old password for User: user", exception.getMessage());
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void authenticateUser_IncrementsFailedAttempts_OnWrongPassword() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("hashed");
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);

        when(userDAO.findByUsername("user")).thenReturn(user);

        passwordUtilMock.when(() -> PasswordUtil.checkPassword("wrong", "hashed")).thenReturn(false);

        boolean result = authService.authenticateUser("user", "wrong");

        assertFalse(result);
        assertEquals(1, user.getFailedLoginAttempts());
        verify(userDAO).update(user);
    }

    @Test
    void authenticateUser_LocksUser_After3FailedAttempts() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("hashed");
        user.setIsActive(true);
        user.setFailedLoginAttempts(2);

        when(userDAO.findByUsername("user")).thenReturn(user);
        passwordUtilMock.when(() -> PasswordUtil.checkPassword("wrong", "hashed")).thenReturn(false);

        boolean result = authService.authenticateUser("user", "wrong");

        assertFalse(result);
        assertEquals(3, user.getFailedLoginAttempts());
        assertNotNull(user.getLockTime(), "User should be locked (lockTime set)");
        verify(userDAO).update(user);
    }

    @Test
    void authenticateUser_ResetsAttempts_OnSuccess() {
        User user = new User();
        user.setUsername("user");
        user.setPassword("hashed");
        user.setIsActive(true);
        user.setFailedLoginAttempts(2);

        when(userDAO.findByUsername("user")).thenReturn(user);
        passwordUtilMock.when(() -> PasswordUtil.checkPassword("right", "hashed")).thenReturn(true);

        boolean result = authService.authenticateUser("user", "right");

        assertTrue(result);
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockTime());
        verify(userDAO).update(user);
    }
}