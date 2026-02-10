package com.civiccomplaint.complaint;

import com.civiccomplaint.common.dto.ApiResponse;
import com.civiccomplaint.complaint.dto.ComplaintCreateRequest;
import com.civiccomplaint.complaint.dto.ComplaintResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.civiccomplaint.common.dto.PaginatedResponse;

/**
 * REST controller for citizen complaint operations.
 */
@Slf4j
@RestController
@RequestMapping("/citizen/complaints")
@RequiredArgsConstructor
public class ComplaintController {

        private final ComplaintService complaintService;

        /**
         * Create a new complaint.
         * Only authenticated citizens can create complaints.
         *
         * @param request        complaint creation request
         * @param authentication Spring Security authentication object
         * @return complaint response
         */
        @PostMapping(value = "/create", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAuthority('ROLE_CITIZEN')")
        public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
                        @RequestPart("complaint") @Valid ComplaintCreateRequest request,
                        @RequestPart(value = "files", required = false) List<org.springframework.web.multipart.MultipartFile> files,
                        Authentication authentication) {
                // Extract userId from JWT (stored in authentication principal)
                com.civiccomplaint.auth.CustomUserDetails userDetails = (com.civiccomplaint.auth.CustomUserDetails) authentication
                                .getPrincipal();
                UUID userId = userDetails.getId();

                log.info("POST /citizen/complaints/create - User: {}", userId);

                ComplaintResponse response = complaintService.createComplaint(request, userId, files);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Complaint created successfully", response));
        }

        /**
         * Get all complaints for the authenticated citizen.
         *
         * @param page           page number (0-based)
         * @param size           page size
         * @param status         optional status filter
         * @param authentication Spring Security authentication object
         * @return paginated list of complaints
         */
        @GetMapping
        @PreAuthorize("hasRole('CITIZEN')")
        public ResponseEntity<ApiResponse<PaginatedResponse<ComplaintResponse>>> getMyComplaints(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) ComplaintStatus status,
                        Authentication authentication) {
                com.civiccomplaint.auth.CustomUserDetails userDetails = (com.civiccomplaint.auth.CustomUserDetails) authentication
                                .getPrincipal();
                UUID userId = userDetails.getId();
                log.info("GET /citizen/complaints - User: {}, Page: {}, Size: {}, Status: {}", userId, page, size,
                                status);

                Pageable pageable = PageRequest.of(page, size);
                PaginatedResponse<ComplaintResponse> complaints = complaintService.getComplaintsForCitizen(userId,
                                status, pageable);

                return ResponseEntity.ok(ApiResponse.success(complaints));
        }

        /**
         * Upload images for a complaint.
         *
         * @param complaintId    complaint ID
         * @param files          list of image files
         * @param authentication Spring Security authentication
         * @return list of image URLs
         */
        @PostMapping("/{complaintId}/images")
        @PreAuthorize("hasRole('CITIZEN')")
        public ResponseEntity<ApiResponse<List<String>>> uploadImages(
                        @PathVariable UUID complaintId,
                        @RequestParam("files") List<org.springframework.web.multipart.MultipartFile> files,
                        Authentication authentication) {
                com.civiccomplaint.auth.CustomUserDetails userDetails = (com.civiccomplaint.auth.CustomUserDetails) authentication
                                .getPrincipal();
                UUID userId = userDetails.getId();
                log.info("POST /citizen/complaints/{}/images - User: {}", complaintId, userId);

                List<String> imageUrls = complaintService.addImagesToComplaint(complaintId, userId, files);

                return ResponseEntity.ok(ApiResponse.success("Images uploaded successfully", imageUrls));
        }
}
