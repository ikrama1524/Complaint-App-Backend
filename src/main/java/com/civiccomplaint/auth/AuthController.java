package com.civiccomplaint.auth;

import com.civiccomplaint.auth.dto.AuthResponse;
import com.civiccomplaint.auth.dto.LoginRequest;
import com.civiccomplaint.auth.dto.RegisterRequest;
import com.civiccomplaint.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * Handles citizen registration and login for both citizens and admins.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new citizen.
     *
     * @param request registration request
     * @return authentication response with token
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> registerCitizen(
            @Valid @RequestBody RegisterRequest request) {
        log.info("POST /auth/register - Registering new citizen");
        AuthResponse response = authService.registerCitizen(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    /**
     * Login for citizen.
     *
     * @param request login request
     * @return authentication response with token
     */
    @PostMapping("/citizen/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginCitizen(
            @Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/citizen/login - Citizen login attempt");
        AuthResponse response = authService.loginCitizen(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Login for admin.
     *
     * @param request login request
     * @return authentication response with token
     */
    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginAdmin(
            @Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/admin/login - Admin login attempt");
        AuthResponse response = authService.loginAdmin(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
