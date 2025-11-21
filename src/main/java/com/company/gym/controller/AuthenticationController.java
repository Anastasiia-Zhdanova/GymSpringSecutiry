package com.company.gym.controller;

import com.company.gym.dto.request.ChangePasswordRequest;
import com.company.gym.dto.request.LoginRequest;
import com.company.gym.dto.request.TraineeRegistrationRequest;
import com.company.gym.dto.request.TrainerRegistrationRequest;
import com.company.gym.dto.response.AuthResponse;
import com.company.gym.exception.AuthenticationException;
import com.company.gym.security.JwtService;
import com.company.gym.service.AuthService;
import com.company.gym.service.TraineeService;
import com.company.gym.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication and Registration")
public class AuthenticationController {

    private final AuthService authService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthenticationController(AuthService authService,
                                    TraineeService traineeService,
                                    TrainerService trainerService,
                                    JwtService jwtService,
                                    UserDetailsService userDetailsService) {
        this.authService = authService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/trainee/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "1. Trainee Registration", description = "Возвращает сгенерированные Username и Password.")
    public AuthResponse registerTrainee(@Valid @RequestBody TraineeRegistrationRequest request) {
        return traineeService.createProfile(
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth(),
                request.getAddress()
        );
    }

    @PostMapping("/trainer/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "2. Trainer Registration", description = "Возвращает сгенерированные Username и Password.")
    public AuthResponse registerTrainer(@Valid @RequestBody TrainerRegistrationRequest request) {
        return trainerService.createProfile(
                request.getFirstName(),
                request.getLastName(),
                request.getSpecializationId()
        );
    }

    @PostMapping("/login")
    @Operation(summary = "3. Login", description = "Аутентификация и генерация JWT токена.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content =
            @Content(schema = @Schema(implementation = LoginRequest.class))))
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        if (!authService.authenticateUser(request.getUsername(), request.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        String jwtToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(Map.of("token", jwtToken));
    }

    @PutMapping("/change-password")
    @Operation(summary = "4. Change Login (Password)", description = "Требуется аутентификация. Idempotent.")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        if (userDetails == null || !userDetails.getUsername().equals(request.getUsername())) {
            throw new AuthenticationException("Forbidden. You can only change your own password.");
        }

        authService.changePassword(
                request.getUsername(),
                request.getOldPassword(),
                request.getNewPassword()
        );

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @Operation(description = "Logout current user (Client should remove JWT)")
    @ResponseStatus(HttpStatus.OK)
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}