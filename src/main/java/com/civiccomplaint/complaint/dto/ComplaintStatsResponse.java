package com.civiccomplaint.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintStatsResponse {
    private long total;
    private long pending;
    private long inProgress;
    private long resolved;
}
