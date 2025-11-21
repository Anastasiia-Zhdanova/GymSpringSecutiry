package com.company.gym.service;

import com.company.gym.dao.UserDAO;
import com.company.gym.entity.User;
import com.company.gym.exception.AuthenticationException;
import com.company.gym.exception.ValidationException;
import com.company.gym.util.PasswordUtil;
import com.company.gym.util.UserCredentialGenerator;
import com.company.gym.util.UsernameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Date;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCK_TIME_DURATION = 5 * 60 * 1000; // 5 minutes

    private final UserDAO userDAO;
    private final Counter registrationCounter;
    public AuthService(UserDAO userDAO, MeterRegistry registry) {
        this.userDAO = userDAO;
        this.registrationCounter = Counter.builder("app.user.registration.total").description("Total number of user registrations").register(registry);
    }

    @Transactional
    public <T extends User> String assignUniqueUsernameAndPassword(T user) {
        logger.info("Generating unique username and password for user: {} {}", user.getFirstName(), user.getLastName());

        String baseUsername = generateUserName(user.getFirstName(), user.getLastName());
        String uniqueUsername = baseUsername;
        int counter = 0;

        while (isUsernameTaken(uniqueUsername)) {
            counter++;
            uniqueUsername = baseUsername + counter;
        }

        user.setUsername(uniqueUsername);
        var plainPassword = UserCredentialGenerator.generatePassword();
        user.setPassword(PasswordUtil.hashPassword(plainPassword));
        user.setIsActive(true);

        logger.info("Assigned username: {}", user.getUsername());

        registrationCounter.increment();

        return plainPassword;
    }

    public boolean isUsernameTaken(String username) {
        return userDAO.findByUsername(username) != null;
    }

    private String generateUserName(String firstName, String lastName) {
        try {
            return UsernameUtil.generateBaseUsername(firstName, lastName);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    @Transactional
    public boolean authenticateUser(String username, String password) {
        User user = userDAO.findByUsername(username);

        if (user == null) {
            logger.warn("Authentication failed: User '{}' not found.", username);
            return false;
        }

        if (!user.getIsActive()) {
            logger.warn("Authentication failed: User '{}' is deactivated.", username);
            return false;
        }

        if (user.getLockTime() != null) {
            long lockTimeInMillis = user.getLockTime().getTime();
            long currentTimeInMillis = System.currentTimeMillis();

            if (lockTimeInMillis + LOCK_TIME_DURATION > currentTimeInMillis) {
                logger.warn("User '{}' is locked due to brute force attempts.", username);
                throw new AuthenticationException("User is locked. Try again in 5 minutes.");
            } else {
                user.setLockTime(null);
                user.setFailedLoginAttempts(0);
                userDAO.update(user);
            }
        }

        boolean isAuthenticated = PasswordUtil.checkPassword(password, user.getPassword());

        if (isAuthenticated) {
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                user.setLockTime(null);
                userDAO.update(user);
            }
            logger.info("User '{}' authenticated successfully.", username);
            return true;
        } else {
            int newFailCount = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(newFailCount);

            if (newFailCount >= MAX_FAILED_ATTEMPTS) {
                user.setLockTime(new Date());
                logger.warn("User '{}' locked due to 3 failed login attempts.", username);
            }

            userDAO.update(user);
            logger.warn("Authentication failed: Invalid password for User '{}'. Attempts: {}", username, newFailCount);
            return false;
        }
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userDAO.findByUsername(username);
        if (user == null) {
            throw new ValidationException("User profile not found for password change: " + username);
        }

        if (!PasswordUtil.checkPassword(oldPassword, user.getPassword())) {
            throw new ValidationException("Incorrect old password for User: " + username);
        }

        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        user.setPassword(hashedPassword);
        userDAO.update(user);
        logger.info("User '{}' password changed successfully.", username);
    }
}