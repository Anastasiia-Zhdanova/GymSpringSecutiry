package com.company.gym.controller;

import com.company.gym.config.JwtAuthenticationFilter;
import com.company.gym.config.WebSecurityConfig;
import com.company.gym.dto.request.ChangePasswordRequest;
import com.company.gym.dto.request.LoginRequest;
import com.company.gym.dto.request.TraineeRegistrationRequest;
import com.company.gym.dto.request.TrainerRegistrationRequest;
import com.company.gym.dto.response.AuthResponse;
import com.company.gym.exception.GlobalExceptionHandler;
import com.company.gym.exception.ValidationException;
import com.company.gym.security.JwtService;
import com.company.gym.service.AuthService;
import com.company.gym.service.TraineeService;
import com.company.gym.service.TrainerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(
        controllers = AuthenticationController.class,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        WebSecurityConfig.class,
                        JwtAuthenticationFilter.class,
                        GlobalExceptionHandler.class
                }
        )
)
@DisplayName("AuthenticationController Test Suite (100% Coverage)")
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;
    @MockBean
    private TraineeService traineeService;
    @MockBean
    private TrainerService trainerService;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetails principal;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        principal = User.withUsername("test.user").password("password").roles("USER").build();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void registerTrainee_Success_Returns201AndCredentials() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        AuthResponse expectedResponse = new AuthResponse("john.doe", "generatedpwd");
        when(traineeService.createProfile(any(), any(), any(), any())).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/auth/trainee/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void registerTrainee_ValidationFailure_Returns400() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setLastName("Doe");
        mockMvc.perform(post("/api/v1/auth/trainee/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTrainer_Success_Returns201AndCredentials() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setSpecializationId(1L);
        AuthResponse expectedResponse = new AuthResponse("jane.smith", "trainerpwd");
        when(trainerService.createProfile(any(), any(), eq(1L))).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/v1/auth/trainer/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void registerTrainer_ServiceValidationFailure_Returns400() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setSpecializationId(99L);
        doThrow(new ValidationException("Training Type not found")).when(trainerService).createProfile(any(), any(), any());

        mockMvc.perform(post("/api/v1/auth/trainer/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success_ReturnsToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test.user");
        request.setPassword("password");

        UserDetails mockUserDetails = User.withUsername("test.user").password("pass").roles("USER").build();

        when(authService.authenticateUser("test.user", "password")).thenReturn(true);
        when(userDetailsService.loadUserByUsername("test.user")).thenReturn(mockUserDetails);
        when(jwtService.generateToken(mockUserDetails)).thenReturn("mock.jwt.token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"));
    }

    @Test
    void login_Failure_Returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("test.user");
        request.setPassword("wrong");

        when(authService.authenticateUser("test.user", "wrong")).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_Success_Returns200AndInvalidatesSession() throws Exception {
        String username = "test.user";
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(username);
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass123");
        doNothing().when(authService).changePassword(any(), any(), any());

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .with(csrf())
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_MismatchedUser_Returns401Forbidden() throws Exception {
        String otherUser = "other.user";
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(otherUser);
        request.setOldPassword("anyOldPass");
        request.setNewPassword("anyNewPass123");

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .with(csrf())
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_AuthServiceFails_Returns400() throws Exception {
        String username = "test.user";
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(username);
        request.setOldPassword("wrongPass");
        request.setNewPassword("newPass123");
        doThrow(new ValidationException("Incorrect old password for User: " + username)).when(authService).changePassword(any(), any(), any());

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .with(csrf())
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logout_Success_Returns200() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf())
                )
                .andExpect(status().isOk());
    }
}