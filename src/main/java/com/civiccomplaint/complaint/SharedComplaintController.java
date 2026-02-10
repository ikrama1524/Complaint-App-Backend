package com.civiccomplaint.complaint;

import com.civiccomplaint.common.dto.ApiResponse;
import com.civiccomplaint.complaint.dto.ComplaintDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class SharedComplaintController {

    private final ComplaintService complaintService;

    /**
     * Get detailed view of a complaint.
     * Accessible by CITIZEN, ADMIN, SUPER_ADMIN.
     * Access control logic is handled in the service layer.
     *
     * @param complaintId    the complaint ID
     * @param authentication authenticated user
     * @return full complaint details
     */
    @GetMapping("/{complaintId}")
    @PreAuthorize("isAuthenticated()") // Basic check, roles filtered in service
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> getComplaintDetails(
            @PathVariable UUID complaintId,
            Authentication authentication) {

        com.civiccomplaint.auth.CustomUserDetails userDetails = (com.civiccomplaint.auth.CustomUserDetails) authentication
                .getPrincipal();
        UUID userId = userDetails.getId();

        log.info("GET /api/complaints/{} - User: {}", complaintId, userId);

        ComplaintDetailResponse response = complaintService.getComplaintDetails(complaintId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
