package com.civiccomplaint.complaint;

import com.civiccomplaint.common.entity.BaseEntity;
import com.civiccomplaint.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Complaint entity representing a civic complaint submitted by a citizen.
 */
@Entity
@Table(name = "complaints", indexes = {
        @Index(name = "idx_complaints_user_id", columnList = "user_id"),
        @Index(name = "idx_complaints_status", columnList = "status"),
        @Index(name = "idx_complaints_type", columnList = "complaint_type"),
        @Index(name = "idx_complaints_created_at", columnList = "created_at"),
        @Index(name = "idx_complaints_user_status", columnList = "user_id, status"),
        @Index(name = "idx_complaints_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_complaints_status_created", columnList = "status, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint extends BaseEntity {

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_complaints_user"))
    private User user;

    @Column(name = "complaint_number", nullable = false, unique = true)
    private String complaintNumber;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Complaint type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "complaint_type", nullable = false, columnDefinition = "complaint_type")
    @org.hibernate.annotations.Type(com.civiccomplaint.common.type.PostgreSQLEnumType.class)
    private ComplaintType complaintType;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "complaint_status")
    @org.hibernate.annotations.Type(com.civiccomplaint.common.type.PostgreSQLEnumType.class)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.PENDING;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "location_text", columnDefinition = "TEXT")
    private String locationText;

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ComplaintAttachment> attachments = new ArrayList<>();

    public void addAttachment(ComplaintAttachment attachment) {
        attachments.add(attachment);
        attachment.setComplaint(this);
    }

    public void removeAttachment(ComplaintAttachment attachment) {
        attachments.remove(attachment);
        attachment.setComplaint(null);
    }
}
