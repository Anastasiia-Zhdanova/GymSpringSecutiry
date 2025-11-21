package com.company.gym.controller;

import com.company.gym.config.JwtAuthenticationFilter;
import com.company.gym.config.WebSecurityConfig;
import com.company.gym.dto.request.TrainingRequest;
import com.company.gym.exception.AuthenticationException;
import com.company.gym.security.JwtService;
import com.company.gym.service.AuthService;
import com.company.gym.service.TrainingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
@Import({WebSecurityConfig.class, JwtAuthenticationFilter.class})
public class TrainingControllerTest {

    private static final String TRAINEE_USERNAME = "trainee.john";
    private static final String TRAINER_USERNAME = "trainer.jane";
    private static final String OTHER_USERNAME = "random.user";
    private static final String BASE_URL = "/api/v1/trainings";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainingService trainingService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private TrainingRequest validRequest;
    private UserDetails traineePrincipal;
    private UserDetails trainerPrincipal;

    @BeforeEach
    void setUp() {
        traineePrincipal = User.withUsername(TRAINEE_USERNAME).password("pass").roles("USER").build();
        trainerPrincipal = User.withUsername(TRAINER_USERNAME).password("pass").roles("USER").build();

        validRequest = new TrainingRequest();
        validRequest.setTraineeUsername(TRAINEE_USERNAME);
        validRequest.setTrainerUsername(TRAINER_USERNAME);
        validRequest.setTrainingName("Strength");
        validRequest.setTrainingDate(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)));
        validRequest.setTrainingDuration(60);
    }

    @Test
    void createTraining_Success_AuthenticatedAsTrainee() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(user(traineePrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());

        verify(trainingService, times(1)).createTraining(
                eq(TRAINEE_USERNAME), eq(TRAINER_USERNAME), any(), any(), eq(60));
    }

    @Test
    void createTraining_AccessDenied_WhenUserIsNotInvolved() throws Exception {
        UserDetails otherPrincipal = User.withUsername(OTHER_USERNAME).password("pass").roles("USER").build();

        mockMvc.perform(post(BASE_URL)
                        .with(user(otherPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AuthenticationException));

        verify(trainingService, never()).createTraining(any(), any(), any(), any(), any());
    }

    @Test
    void createTraining_AccessDenied_WhenUnauthenticated() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }
}