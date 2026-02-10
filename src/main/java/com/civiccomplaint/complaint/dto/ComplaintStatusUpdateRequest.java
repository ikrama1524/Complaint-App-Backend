package com.civiccomplaint.complaint.dto;

import com.civiccomplaint.complaint.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating complaint status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ComplaintStatus status;
}
