package com.civiccomplaint.user;

import com.civiccomplaint.common.dto.ApiResponse;
import com.civiccomplaint.user.dto.CreateAdminRequest;
import com.civiccomplaint.user.dto.UpdateAdminRequest;
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

import com.civiccomplaint.master.CorporatorService;
import com.civiccomplaint.master.dto.CorporatorResponse;
import com.civiccomplaint.user.dto.CreateUserFromCorporatorRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserService userService;
    private final com.civiccomplaint.complaint.ComplaintService complaintService;
    private final CorporatorService corporatorService;

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
     * Update an existing Admin user.
     * Only accessible by SUPER_ADMIN.
     *
     * @param id      admin user ID
     * @param request update request
     * @return updated user response
     */
    @org.springframework.web.bind.annotation.PutMapping("/admins/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateAdmin(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAdminRequest request) {
        log.info("PUT /api/super-admin/admins/{} - Updating admin user", id);
        UserResponse updatedAdmin = userService.updateAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success("Admin user updated successfully", updatedAdmin));
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
            @PathVariable UUID adminId,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /api/super-admin/admins/{}/poster - Uploading poster image", adminId);
        userService.uploadAdminPoster(adminId, file);
        return ResponseEntity.ok(ApiResponse.success("Poster image uploaded successfully", null));
    }

    /**
     * Get a poster image for an admin.
     * Only accessible by SUPER_ADMIN.
     *
     * @param adminId admin user ID
     * @return image bytes
     */
    @GetMapping("/admins/{adminId}/poster")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<byte[]> getAdminPoster(@PathVariable UUID adminId) {
        log.info("GET /api/super-admin/admins/{}/poster - Fetching poster image", adminId);
        org.springframework.data.util.Pair<byte[], String> posterData = userService.getAdminPoster(adminId);

        String contentType = posterData.getSecond();
        if (contentType == null) {
            contentType = "image/jpeg"; // Default fallback
        }

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, contentType)
                .body(posterData.getFirst());
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
    @GetMapping("/complaints")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<com.civiccomplaint.common.dto.PaginatedResponse<com.civiccomplaint.complaint.dto.ComplaintResponse>>> getComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID adminId,
            @RequestParam(required = false) Integer prabhagId,
            @RequestParam(required = false) com.civiccomplaint.complaint.ComplaintStatus status) {
        log.info("GET /api/super-admin/complaints - Page: {}, Size: {}, AdminId: {}, PrabhagId: {}, Status: {}", page,
                size, adminId, prabhagId, status);

        Pageable pageable = PageRequest.of(page, size);
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
    @GetMapping("/admins")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllAdmins() {
        log.info("GET /api/super-admin/admins - Fetching all admins");
        List<UserResponse> admins = userService.getAllAdmins();
        return ResponseEntity.ok(ApiResponse.success(admins));
    }

    /**
     * Get all corporators.
     * Only accessible by SUPER_ADMIN.
     *
     * @return list of corporators
     */
    @GetMapping("/corporators")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<CorporatorResponse>>> getAllCorporators() {
        log.info("GET /api/super-admin/corporators - Fetching all corporators");
        List<CorporatorResponse> corporators = corporatorService.getAllCorporators();
        return ResponseEntity.ok(ApiResponse.success(corporators));
    }

    /**
     * Create a user account for a corporator.
     * Only accessible by SUPER_ADMIN.
     *
     * @param id      corporator ID
     * @param request request containing password
     * @return created user response
     */
    @PostMapping(value = "/corporators/{id}/create-user", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUserFromCorporator(
            @PathVariable Integer id,
            @Valid @org.springframework.web.bind.annotation.ModelAttribute CreateUserFromCorporatorRequest request) {
        log.info("POST /api/super-admin/corporators/{}/create-user - Creating user for corporator", id);
        UserResponse createdUser = userService.createAdminFromCorporator(id, request);
        return ResponseEntity.ok(ApiResponse.success("User account created successfully", createdUser));
    }
}
