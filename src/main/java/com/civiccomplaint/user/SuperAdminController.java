package com.civiccomplaint.user;

import com.civiccomplaint.common.dto.ApiResponse;
import com.civiccomplaint.user.dto.CreateAdminRequest;
import com.civiccomplaint.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserService userService;
    private final com.civiccomplaint.complaint.ComplaintService complaintService;

    /**
     * Create a new Admin user.
     * Only accessible by SUPER_ADMIN.
     *
     * @param request admin creation request
     * @return created user response
     */
    @PostMapping("/admins")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        log.info("POST /api/super-admin/admins - Creating admin user: {}", request.getEmail());
        UserResponse createdAdmin = userService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin user created successfully", createdAdmin));
    }

    /**
     * Upload a poster image for an admin.
     * Only accessible by SUPER_ADMIN.
     *
     * @param adminId admin user ID
     * @param file    image file
     * @return success response
     */
    @PostMapping("/admins/{adminId}/poster")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> uploadAdminPoster(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID adminId,
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        log.info("POST /api/super-admin/admins/{}/poster - Uploading poster image", adminId);
        userService.uploadAdminPoster(adminId, file);
        return ResponseEntity.ok(ApiResponse.success("Poster image uploaded successfully", null));
    }

    /**
     * Get complaints for Super Admin with optional filters.
     *
     * @param page      page number
     * @param size      page size
     * @param adminId   optional filter by admin ID
     * @param prabhagId optional filter by prabhag ID
     * @param status    optional filter by status
     * @return paginated complaints
     */
    @org.springframework.web.bind.annotation.GetMapping("/complaints")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<com.civiccomplaint.common.dto.PaginatedResponse<com.civiccomplaint.complaint.dto.ComplaintResponse>>> getComplaints(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") int size,
            @org.springframework.web.bind.annotation.RequestParam(required = false) java.util.UUID adminId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) java.util.UUID prabhagId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) com.civiccomplaint.complaint.ComplaintStatus status) {
        log.info("GET /api/super-admin/complaints - Page: {}, Size: {}, AdminId: {}, PrabhagId: {}, Status: {}", page,
                size, adminId, prabhagId, status);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        com.civiccomplaint.common.dto.PaginatedResponse<com.civiccomplaint.complaint.dto.ComplaintResponse> response = complaintService
                .getComplaintsForSuperAdmin(adminId, prabhagId, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all admin users.
     * Only accessible by SUPER_ADMIN.
     *
     * @return list of admin users
     */
    @org.springframework.web.bind.annotation.GetMapping("/admins")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<java.util.List<UserResponse>>> getAllAdmins() {
        log.info("GET /api/super-admin/admins - Fetching all admins");
        java.util.List<UserResponse> admins = userService.getAllAdmins();
        return ResponseEntity.ok(ApiResponse.success(admins));
    }
}
