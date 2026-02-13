package com.civiccomplaint.master;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a Corporator (Elected Representative for a Prabhag).
 */
@Entity(name = "corporator")
@Table(name = "corporator", indexes = {
        @Index(name = "idx_corporator_prabhag_id", columnList = "prabhag_id"),
        @Index(name = "idx_corporator_email", columnList = "email"),
        @Index(name = "idx_corporator_mobile", columnList = "mobile_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Corporator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank(message = "Full name is required")
    @Size(max = 200, message = "Full name must not exceed 200 characters")
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Mobile number must be valid")
    @Size(max = 20, message = "Mobile number must not exceed 20 characters")
    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @NotNull(message = "Prabhag is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prabhag_id", nullable = false)
    private Prabhag prabhag;

    @Column(name = "is_user_created", nullable = false)
    @Builder.Default
    private Boolean isUserCreated = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
