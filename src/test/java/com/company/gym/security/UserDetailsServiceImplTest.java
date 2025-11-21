package com.company.gym.security;

import com.company.gym.dao.UserDAO;
import com.company.gym.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_Success() {
        User userEntity = new User();
        userEntity.setUsername("john.doe");
        userEntity.setPassword("hashed_pass");
        userEntity.setIsActive(true);

        when(userDAO.findByUsername("john.doe")).thenReturn(userEntity);

        UserDetails result = userDetailsService.loadUserByUsername("john.doe");

        assertNotNull(result);
        assertEquals("john.doe", result.getUsername());
        assertEquals("hashed_pass", result.getPassword());
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonLocked());
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        when(userDAO.findByUsername("unknown")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown"));
    }

    @Test
    void loadUserByUsername_UserLocked_ReturnsLockedDetails() {
        User userEntity = new User();
        userEntity.setUsername("locked.user");
        userEntity.setPassword("pass");
        userEntity.setIsActive(true);
        userEntity.setLockTime(new java.util.Date());

        when(userDAO.findByUsername("locked.user")).thenReturn(userEntity);

        UserDetails result = userDetailsService.loadUserByUsername("locked.user");

        assertFalse(result.isAccountNonLocked());
    }
}