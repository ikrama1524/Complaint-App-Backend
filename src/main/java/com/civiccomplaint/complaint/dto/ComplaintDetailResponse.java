package com.civiccomplaint.complaint.dto;

import com.civiccomplaint.complaint.ComplaintStatus;
import com.civiccomplaint.complaint.ComplaintType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ComplaintDetailResponse {
    // Complaint Info
    private UUID id;
    private String complaintNumber;
    private String title;
    private String description;
    private ComplaintType complaintType;
    private ComplaintStatus status;
    private LocalDateTime createdAt;

    // Raised By (Citizen)
    private CitizenInfo raisedBy;

    // Admin Info (Prabhag context)
    private AdminInfo adminInfo;

    // Location
    private LocationInfo location;

    // Attachments
    private List<AttachmentInfo> attachments;

    @Data
    @Builder
    public static class CitizenInfo {
        private UUID id;
        private String fullName;
        private String mobileNumber;
        private String email;
        private String address;
        private String pinCode;
        private String prabhagName;
    }

    @Data
    @Builder
    public static class AdminInfo {
        private Integer prabhagId;
        private String prabhagName;
        // In this system, complaints are assigned to a Prabhag, managed by Admins of
        // that Prabhag
    }

    @Data
    @Builder
    public static class LocationInfo {
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String locationText;
    }

    @Data
    @Builder
    public static class AttachmentInfo {
        private UUID id;
        private String url;
        private String contentType;
    }
}
