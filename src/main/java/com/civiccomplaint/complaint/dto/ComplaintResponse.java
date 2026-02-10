package com.civiccomplaint.complaint.dto;

import com.civiccomplaint.complaint.ComplaintStatus;
import com.civiccomplaint.complaint.ComplaintType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for complaint response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {

    private UUID id;
    private String complaintNumber;
    private String title;
    private String description;
    private ComplaintType complaintType;
    private ComplaintStatus status;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationText;
    private LocalDateTime createdAt;
    private java.util.List<String> imageUrls;
}
