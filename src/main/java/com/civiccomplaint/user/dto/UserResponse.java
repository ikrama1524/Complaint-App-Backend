package com.civiccomplaint.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.civiccomplaint.master.dto.PrabhagResponse;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String role;
    private Boolean isActive;
    private String address;
    private String pinCode;
    private Boolean hasPoster;
    private PrabhagResponse prabhag;
}
