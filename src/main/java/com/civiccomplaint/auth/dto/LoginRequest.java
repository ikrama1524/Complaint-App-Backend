package com.civiccomplaint.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login request.
 * Can be used for both citizen and admin login.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email or mobile number is required")
    private String identifier; // Can be email or mobile number

    @NotBlank(message = "Password is required")
    private String password;
}
