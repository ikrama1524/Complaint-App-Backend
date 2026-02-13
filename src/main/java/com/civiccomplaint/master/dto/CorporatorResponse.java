package com.civiccomplaint.master.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for Corporator response.
 */
@Data
@Builder
public class CorporatorResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String mobileNumber;
    private Integer prabhagId;
    private String prabhagName;
    private Boolean isUserCreated;
}
