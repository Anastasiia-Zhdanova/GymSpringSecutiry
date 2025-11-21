package com.company.gym.controller;

import com.company.gym.config.JwtAuthenticationFilter;
import com.company.gym.config.WebSecurityConfig;
import com.company.gym.dto.request.TrainerProfileUpdateRequest;
import com.company.gym.dto.request.UserStatusUpdateRequest;
import com.company.gym.dto.response.TrainerProfileResponse;
import com.company.gym.dto.response.TrainerShortResponse;
import com.company.gym.dto.response.TrainingListResponse;
import com.company.gym.exception.AuthenticationException;
import com.company.gym.security.JwtService;
import com.company.gym.service.AuthService;
import com.company.gym.service.TrainerServiceFacade;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerController.class)
@Import({WebSecurityConfig.class, JwtAuthenticationFilter.class})
public class TrainerControllerTest {

    private static final String TRAINER_USERNAME = "trainer.user";
    private static final String OTHER_USERNAME = "other.user";
    private static final String BASE_URL = "/api/v1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainerServiceFacade trainerServiceFacade;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetails mockPrincipal;
    private TrainerProfileResponse mockProfileResponse;
    private TrainerProfileUpdateRequest mockUpdateRequest;

    @BeforeEach
    void setUp() {
        mockPrincipal = User.withUsername(TRAINER_USERNAME).password("pass").roles("USER").build();

        mockProfileResponse = new TrainerProfileResponse();
        mockProfileResponse.setUsername(TRAINER_USERNAME);

        mockUpdateRequest = new TrainerProfileUpdateRequest();
        mockUpdateRequest.setFirstName("New");
        mockUpdateRequest.setLastName("Name");
        mockUpdateRequest.setIsActive(true);
        mockUpdateRequest.setSpecializationId(1L);
    }

    @Test
    void getProfile_Success() throws Exception {
        when(trainerServiceFacade.getProfile(TRAINER_USERNAME)).thenReturn(mockProfileResponse);

        mockMvc.perform(get(BASE_URL + "/trainers/{username}", TRAINER_USERNAME)
                        .with(user(mockPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TRAINER_USERNAME));
    }

    @Test
    void updateProfile_Success() throws Exception {
        when(trainerServiceFacade.updateProfile(eq(TRAINER_USERNAME), any(TrainerProfileUpdateRequest.class)))
                .thenReturn(mockProfileResponse);

        mockMvc.perform(put(BASE_URL + "/trainers/{username}", TRAINER_USERNAME)
                        .with(user(mockPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUpdateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void getUnassignedTrainers_Success() throws Exception {
        UserDetails traineePrincipal = User.withUsername("trainee.user").password("pass").roles("USER").build();
        TrainerShortResponse mockTrainerShort = new TrainerShortResponse();
        mockTrainerShort.setUsername("unassigned.trainer");

        when(trainerServiceFacade.getUnassignedTrainers("trainee.user")).thenReturn(List.of(mockTrainerShort));

        mockMvc.perform(get(BASE_URL + "/trainees/{traineeUsername}/unassigned-trainers", "trainee.user")
                        .with(user(traineePrincipal)))
                .andExpect(status().isOk());
    }

    @Test
    void getTrainings_Success() throws Exception {
        when(trainerServiceFacade.getTrainings(eq(TRAINER_USERNAME), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/trainers/{username}/trainings", TRAINER_USERNAME)
                        .with(user(mockPrincipal)))
                .andExpect(status().isOk());
    }
}