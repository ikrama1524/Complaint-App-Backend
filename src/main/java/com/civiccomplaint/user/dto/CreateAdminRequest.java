package com.civiccomplaint.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdminRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Mobile number must be 10-15 digits")
    private String mobileNumber;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "PIN code is required")
    private String pinCode;

    @NotNull(message = "Prabhag ID is required for Admin")
    private Integer prabhagId;
}
