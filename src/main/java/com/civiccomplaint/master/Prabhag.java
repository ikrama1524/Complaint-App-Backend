package com.civiccomplaint.master;

import com.civiccomplaint.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entity representing a Prabhag (administrative ward/zone).
 */
@Entity
@Table(name = "prabhags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prabhag extends BaseEntity {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Code is required")
    @Size(max = 10, message = "Code must not exceed 10 characters")
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
