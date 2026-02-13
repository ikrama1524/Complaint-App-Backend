package com.civiccomplaint.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.web.multipart.MultipartFile;

/**
 * Request DTO for creating a user from a corporator.
 */
@Data
@NoArgsConstructor
public class CreateUserFromCorporatorRequest {

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String email;

    private String mobileNumber;

    private MultipartFile posterImage;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Pin Code is required")
    private String pinCode;
}
