package com.company.gym.controller;

import com.company.gym.config.JwtAuthenticationFilter;
import com.company.gym.config.WebSecurityConfig;
import com.company.gym.dto.request.TraineeProfileUpdateRequest;
import com.company.gym.dto.request.UpdateTraineeTrainersRequest;
import com.company.gym.dto.request.UserStatusUpdateRequest;
import com.company.gym.dto.response.TraineeProfileResponse;
import com.company.gym.dto.response.TrainerShortResponse;
import com.company.gym.dto.response.TrainingListResponse;
import com.company.gym.exception.AuthenticationException;
import com.company.gym.security.JwtService;
import com.company.gym.service.AuthService;
import com.company.gym.service.TraineeServiceFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TraineeController.class)
@Import({WebSecurityConfig.class, JwtAuthenticationFilter.class})
public class TraineeControllerTest {

    private static final String TRAINEE_USERNAME = "trainee.user";
    private static final String OTHER_USERNAME = "other.user";
    private static final String BASE_URL = "/api/v1/trainees";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TraineeServiceFacade traineeServiceFacade;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    private UserDetails mockPrincipal;
    private TraineeProfileResponse mockProfileResponse;
    private TraineeProfileUpdateRequest mockUpdateRequest;
    private TrainerShortResponse mockTrainerResponse;

    @BeforeEach
    void setUp() {
        mockPrincipal = User.withUsername(TRAINEE_USERNAME).password("pass").roles("USER").build();

        mockProfileResponse = new TraineeProfileResponse();
        mockProfileResponse.setUsername(TRAINEE_USERNAME);

        mockUpdateRequest = new TraineeProfileUpdateRequest();
        mockUpdateRequest.setFirstName("New");
        mockUpdateRequest.setLastName("Name");
        mockUpdateRequest.setIsActive(true);

        mockTrainerResponse = new TrainerShortResponse();
        mockTrainerResponse.setUsername("trainer.one");
    }

    @Test
    void accessDenied_WhenPrincipalUsernameDoesNotMatchRequestedUsername() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{username}", OTHER_USERNAME)
                        .with(user(mockPrincipal)))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AuthenticationException));
    }

    @Test
    void getProfile_Success() throws Exception {
        when(traineeServiceFacade.getProfile(TRAINEE_USERNAME)).thenReturn(mockProfileResponse);

        mockMvc.perform(get(BASE_URL + "/{username}", TRAINEE_USERNAME)
                        .with(user(mockPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TRAINEE_USERNAME));
    }

    @Test
    void updateProfile_Success() throws Exception {
        when(traineeServiceFacade.updateProfile(eq(TRAINEE_USERNAME), any(TraineeProfileUpdateRequest.class)))
                .thenReturn(mockProfileResponse);

        mockMvc.perform(put(BASE_URL + "/{username}", TRAINEE_USERNAME)
                        .with(user(mockPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUpdateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProfile_Success() throws Exception {
        when(traineeServiceFacade.deleteProfile(TRAINEE_USERNAME)).thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete(BASE_URL + "/{username}", TRAINEE_USERNAME)
                        .with(user(mockPrincipal)))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateTrainers_Success() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setTrainerUsernames(Set.of("trainer.one"));

        when(traineeServiceFacade.updateTrainers(eq(TRAINEE_USERNAME), any(UpdateTraineeTrainersRequest.class)))
                .thenReturn(List.of(mockTrainerResponse));

        mockMvc.perform(put(BASE_URL + "/{username}/trainers", TRAINEE_USERNAME)
                        .with(user(mockPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_Success() throws Exception {
        UserStatusUpdateRequest request = new UserStatusUpdateRequest();
        request.setIsActive(false);

        mockMvc.perform(patch(BASE_URL + "/{username}/status", TRAINEE_USERNAME)
                        .with(user(mockPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}