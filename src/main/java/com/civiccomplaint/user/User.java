package com.civiccomplaint.user;

import com.civiccomplaint.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * User entity representing both citizens and administrators.
 */
@Entity
@Table(name = "users", indexes = {
                @Index(name = "idx_users_role", columnList = "role"),
                @Index(name = "idx_users_mobile", columnList = "mobileNumber"),
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_pin_code", columnList = "pinCode"),
                @Index(name = "idx_users_is_active", columnList = "isActive")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_mobile", columnNames = "mobileNumber"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

        @NotNull(message = "Role is required")
        @Enumerated(EnumType.STRING)
        @Column(name = "role", nullable = false, columnDefinition = "user_role")
        @org.hibernate.annotations.Type(com.civiccomplaint.common.type.PostgreSQLEnumType.class)
        @Builder.Default
        private Role role = Role.CITIZEN;

        @NotBlank(message = "Full name is required")
        @Column(name = "full_name", nullable = false)
        private String fullName;

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Mobile number must be 10-15 digits, optionally starting with +")
        @Column(name = "mobile_number", nullable = false, unique = true, length = 15)
        private String mobileNumber;

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Column(name = "email", nullable = false, unique = true)
        private String email;

        @NotBlank(message = "Address is required")
        @Column(name = "address", nullable = false, columnDefinition = "TEXT")
        private String address;

        @NotBlank(message = "PIN code is required")
        @Column(name = "pin_code", nullable = false, length = 10)
        private String pinCode;

        @NotBlank(message = "Password is required")
        @Column(name = "password_hash", nullable = false)
        private String password;

        @NotNull(message = "Active status is required")
        @Column(name = "is_active", nullable = false)
        @Builder.Default
        private Boolean isActive = true;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "prabhag_id")
        private com.civiccomplaint.master.Prabhag prabhag;

        @Column(name = "poster_image")
        private byte[] posterImage;

        @Column(name = "poster_image_content_type")
        private String posterImageContentType;
}
