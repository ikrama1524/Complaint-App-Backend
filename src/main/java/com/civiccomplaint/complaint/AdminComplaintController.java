package com.civiccomplaint.complaint;

import com.civiccomplaint.common.dto.ApiResponse;
import com.civiccomplaint.complaint.dto.ComplaintResponse;
import com.civiccomplaint.complaint.dto.ComplaintStatsResponse;
import com.civiccomplaint.complaint.dto.ComplaintStatusUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.civiccomplaint.common.dto.PaginatedResponse;

/**
 * REST controller for admin complaint operations.
 */
@Slf4j
@RestController
@RequestMapping("/admin/complaints")
@RequiredArgsConstructor
public class AdminComplaintController {

        private final ComplaintService complaintService;

        /**
         * Get all complaints in the system.
         * Only accessible by admins.
         *
         * @param page   page number (0-based)
         * @param size   page size
         * @param status optional status filter
         * @return paginated list of all complaints
         */
        @GetMapping("/all")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<PaginatedResponse<ComplaintResponse>>> getAllComplaints(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String status,
                        org.springframework.security.core.Authentication authentication) {
                log.info("GET /admin/complaints/all - Admin access, Page: {}, Size: {}, Status: {}", page, size,
                                status);

                com.civiccomplaint.auth.CustomUserDetails userDetails = (com.civiccomplaint.auth.CustomUserDetails) authentication
                                .getPrincipal();
                UUID adminId = userDetails.getId();
                Pageable pageable = PageRequest.of(page, size);

                ComplaintStatus complaintStatus = null;
                if (status != null && !status.isEmpty()) {
                        try {
                                complaintStatus = ComplaintStatus.valueOf(status);
                        } catch (IllegalArgumentException e) {
                                log.warn("Invalid status parameter: {}", status);
                        }
                }

                PaginatedResponse<ComplaintResponse> complaints = complaintService.getAllComplaintsForAdmin(adminId,
                                complaintStatus, pageable);

                return ResponseEntity.ok(ApiResponse.success(complaints));
        }

        /**
         * Update complaint status.
         * Only accessible by admins.
         *
         * @param complaintId complaint ID
         * @param request     status update request
         * @return updated complaint response
         */
        @PutMapping("/{complaintId}/status")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaintStatus(
                        @PathVariable UUID complaintId,
                        @Valid @RequestBody ComplaintStatusUpdateRequest request,
                        org.springframework.security.core.Authentication authentication) {
                log.info("PUT /admin/complaints/{}/status - Status: {}", complaintId, request.getStatus());

                com.civiccomplaint.auth.CustomUserDetails userDetails = (com.civiccomplaint.auth.CustomUserDetails) authentication
                                .getPrincipal();
                UUID adminId = userDetails.getId();
                ComplaintResponse response = complaintService.updateComplaintStatus(complaintId, adminId, request);

                return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
        }

        /**
         * Get complaint statistics.
         * Only accessible by admins.
         *
         * @return complaint statistics
         */
        @GetMapping("/stats")
        @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiResponse<ComplaintStatsResponse>> getComplaintStats(
                        org.springframework.security.core.Authentication authentication) {
                log.info("GET /admin/complaints/stats - Admin access");
                com.civiccomplaint.auth.CustomUserDetails userDetails = (com.civiccomplaint.auth.CustomUserDetails) authentication
                                .getPrincipal();
                UUID adminId = userDetails.getId();
                ComplaintStatsResponse stats = complaintService.getComplaintStats(adminId);
                return ResponseEntity.ok(ApiResponse.success(stats));
        }
}
