package com.company.gym.config;

import com.company.gym.dto.response.TraineeProfileResponse;
import com.company.gym.security.JwtService;
import com.company.gym.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({WebSecurityConfig.class, JwtAuthenticationFilter.class})
public class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;
    @MockBean private AuthService authService;
    @MockBean private TraineeService traineeService;
    @MockBean private TrainerService trainerService;
    @MockBean private TraineeServiceFacade traineeServiceFacade;
    @MockBean private TrainerServiceFacade trainerServiceFacade;
    @MockBean private TrainingService trainingService;

    @Test
    void publicEndpoints_ShouldBePermitted() throws Exception {
        mockMvc.perform(post("/api/v1/auth/trainee/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectedEndpoint_ShouldReturnForbidden_WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/trainees/test.user"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test.user")
    void protectedEndpoint_ShouldBeAllowed_WhenAuthenticated() throws Exception {
        when(traineeServiceFacade.getProfile("test.user")).thenReturn(new TraineeProfileResponse());

        mockMvc.perform(get("/api/v1/trainees/test.user"))
                .andExpect(status().isOk());
    }

    @Test
    void logoutEndpoint_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk());
    }
}