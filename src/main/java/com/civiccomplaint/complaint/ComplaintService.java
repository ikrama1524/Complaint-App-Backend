package com.civiccomplaint.complaint;

import com.civiccomplaint.common.dto.ApiResponse;
import com.civiccomplaint.complaint.dto.ComplaintCreateRequest;
import com.civiccomplaint.complaint.dto.ComplaintResponse;
import com.civiccomplaint.complaint.dto.ComplaintStatsResponse;
import com.civiccomplaint.complaint.dto.ComplaintStatusUpdateRequest;
import com.civiccomplaint.exception.ResourceNotFoundException;
import com.civiccomplaint.user.Role;
import com.civiccomplaint.user.User;
import com.civiccomplaint.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.civiccomplaint.common.dto.PaginatedResponse;

/**
 * Service for complaint management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ComplaintAttachmentService complaintAttachmentService;
    private final ComplaintSequenceRepository complaintSequenceRepository;

    @org.springframework.beans.factory.annotation.Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Create a new complaint.
     * Only citizens can create complaints.
     *
     * @param request complaint creation request
     * @param userId  ID of the user creating the complaint
     * @param files   list of image files (optional)
     * @return complaint response
     */
    @Transactional
    public ComplaintResponse createComplaint(ComplaintCreateRequest request, UUID userId,
            java.util.List<org.springframework.web.multipart.MultipartFile> files) {
        log.info("Creating complaint for user: {}", userId);

        // Fetch user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Ensure user is a citizen
        if (user.getRole() != Role.CITIZEN) {
            log.warn("Non-citizen user {} attempted to create complaint", userId);
            throw new AccessDeniedException("Only citizens can create complaints");
        }

        // Verify user is active
        if (!user.getIsActive()) {
            log.warn("Inactive user {} attempted to create complaint", userId);
            throw new AccessDeniedException("Account is inactive");
        }

        // Generate Complaint Number
        String complaintNumber = generateComplaintNumber(user);

        // Create complaint entity
        Complaint complaint = Complaint.builder()
                .user(user)
                .complaintNumber(complaintNumber)
                .title(request.getTitle())
                .description(request.getDescription())
                .complaintType(request.getComplaintType())
                .status(ComplaintStatus.PENDING) // Always PENDING on creation
                .latitude(BigDecimal.valueOf(request.getLatitude()))
                .longitude(BigDecimal.valueOf(request.getLongitude()))
                .locationText(request.getLocationText())
                .build();

        // Save complaint
        complaint = complaintRepository.save(complaint);
        log.info("Complaint created successfully with ID: {} and Number: {}", complaint.getId(), complaintNumber);

        // Upload images if any
        if (files != null && !files.isEmpty()) {
            log.info("Uploading {} images for complaint {}", files.size(), complaint.getId());
            complaintAttachmentService.uploadImages(complaint, files);
        }

        // Return response
        return mapToResponse(complaint);
    }

    private String generateComplaintNumber(User user) {
        // Format: CMP-{PRABHAG_CODE}-{YYYY}-{SEQUENCE}
        // Example: CMP-NOR-2024-0001

        com.civiccomplaint.master.Prabhag prabhag = user.getPrabhag();
        String prabhagCode = "GEN"; // Default if not assigned
        Integer prabhagId = null;

        if (prabhag != null) {
            prabhagCode = prabhag.getCode();
            prabhagId = prabhag.getId();
        } else {
            // Need a valid ID for sequence table if using FK constraints.
            // Requirement says "Sequence should be incremental per PRABHAG".
            // If user has no prabhag, we might need a dummy prabhag or handle it
            // differently.
            // For now, let's assume we use a "General" sequence if permitted, but
            // allowing NULL prabhag_id in sequence table might break unique constraint
            // logic.
            // Better approach: Throw error if citizen has no Prabhag, OR use a
            // system-defined default ID.
            // Given earlier tasks ensured Prabhag assignment, let's assume it exists or
            // throw.

            // However, to be safe and robust:
            // If no prabhag, we can't really generate a prabhag-specific sequence.
            // Let's rely on the fact that Registration now mandates Prabhag.
            if (user.getRole() == Role.CITIZEN && prabhag == null) {
                // Determine logic. For now, let's fail-safe to a random suffix or throw.
                // Best correct logic: Fail if Citizen has no Prabhag as per new registration
                // flow.
                log.warn("Citizen {} has no Prabhag assigned. Cannot generate Prabhag-specific ID.", user.getId());
                // Fallback: Use "GEN" and maybe a separate global sequence or random.
                // But wait, the repository needs a UUID.
                // Let's create a 'Default' prabhag or use a hardcoded UUID for 'General'?
                // Easier: Throw exception as data integrity issue.
                throw new IllegalStateException("User must be assigned to a Prabhag to create a complaint.");
            }
        }

        int year = java.time.Year.now().getValue();
        int sequence = complaintSequenceRepository.getNextSequenceValue(prabhagId, year);

        return String.format("CMP-%s-%d-%04d", prabhagCode, year, sequence);
    }

    /**
     * Map Complaint entity to ComplaintResponse DTO.
     *
     * @param complaint complaint entity
     * @return complaint response DTO
     */
    private ComplaintResponse mapToResponse(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .complaintNumber(complaint.getComplaintNumber())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .complaintType(complaint.getComplaintType())
                .status(complaint.getStatus())
                .latitude(complaint.getLatitude())
                .longitude(complaint.getLongitude())
                .locationText(complaint.getLocationText())
                .createdAt(complaint.getCreatedAt())
                .imageUrls(complaint.getAttachments().stream()
                        .map(att -> baseUrl + "/api/complaints/attachments/" + att.getId())
                        .toList())
                .build();
    }

    /**
     * Get all complaints for a specific citizen.
     *
     * @param userId   the user ID
     * @param status   optional status filter
     * @param pageable pagination information
     * @return paginated list of complaints
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ComplaintResponse> getComplaintsForCitizen(UUID userId, ComplaintStatus status,
            Pageable pageable) {
        Page<Complaint> page;

        if (status != null) {
            // Filter by user ID and status
            page = complaintRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else {
            // Get all complaints for user
            page = complaintRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        List<ComplaintResponse> content = page.map(this::mapToResponse).getContent();

        return PaginatedResponse.<ComplaintResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    /**
     * Get all complaints for admin view.
     * Filters by Prabhag for ADMIN role.
     * Returns all for SUPER_ADMIN role.
     *
     * @param adminId  ID of the admin user
     * @param status   optional status filter
     * @param pageable pagination information
     * @return paginated list of complaints
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ComplaintResponse> getAllComplaintsForAdmin(UUID adminId, ComplaintStatus status,
            Pageable pageable) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        Page<Complaint> page;

        if (admin.getRole() == Role.SUPER_ADMIN) {
            if (status != null) {
                page = complaintRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
            } else {
                page = complaintRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        } else if (admin.getRole() == Role.ADMIN) {
            if (admin.getPrabhag() == null) {
                // Should technically not happen if creation flow is strict, but good safety
                page = Page.empty(pageable);
            } else {
                if (status != null) {
                    page = complaintRepository.findByUserPrabhagIdAndStatusOrderByCreatedAtDesc(
                            admin.getPrabhag().getId(), status, pageable);
                } else {
                    page = complaintRepository.findByUserPrabhagIdOrderByCreatedAtDesc(
                            admin.getPrabhag().getId(), pageable);
                }
            }
        } else {
            throw new AccessDeniedException("Unauthorized access");
        }

        List<ComplaintResponse> content = page.map(this::mapToResponse).getContent();

        return PaginatedResponse.<ComplaintResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    /**
     * Update the status of a complaint.
     *
     * @param complaintId the complaint ID
     * @param adminId     ID of the admin updating the status
     * @param request     status update request
     * @return updated complaint response
     */
    @Transactional
    public ComplaintResponse updateComplaintStatus(UUID complaintId, UUID adminId,
            ComplaintStatusUpdateRequest request) {
        log.info("Updating status for complaint: {} by admin: {}", complaintId, adminId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", complaintId));

        // Authorization check for ADMIN (must belong to same Prabhag)
        if (admin.getRole() == Role.ADMIN) {
            if (admin.getPrabhag() == null) {
                throw new AccessDeniedException("Admin is not assigned to any Prabhag");
            }
            // Check if complaint belongs to the admin's prabhag
            // Complaint -> User -> Prabhag
            if (complaint.getUser().getPrabhag() == null ||
                    !complaint.getUser().getPrabhag().getId().equals(admin.getPrabhag().getId())) {
                throw new AccessDeniedException("You can only update complaints within your assigned Prabhag");
            }
        } else if (admin.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Unauthorized access");
        }

        complaint.setStatus(request.getStatus());
        complaint = complaintRepository.save(complaint);

        log.info("Complaint {} status updated to {}", complaintId, request.getStatus());
        return mapToResponse(complaint);
    }

    /**
     * Get complaint statistics.
     *
     * @param adminId ID of the admin user
     * @return complaint statistics
     */
    @Transactional(readOnly = true)
    public ComplaintStatsResponse getComplaintStats(UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        long total;
        long pending;
        long inProgress;
        long resolved;

        if (admin.getRole() == Role.ADMIN) {
            if (admin.getPrabhag() == null) {
                return ComplaintStatsResponse.builder().build();
            }
            Integer prabhagId = admin.getPrabhag().getId();
            total = complaintRepository.countByUserPrabhagId(prabhagId);
            pending = complaintRepository.countByUserPrabhagIdAndStatus(prabhagId, ComplaintStatus.PENDING);
            inProgress = complaintRepository.countByUserPrabhagIdAndStatus(prabhagId, ComplaintStatus.IN_PROGRESS);
            resolved = complaintRepository.countByUserPrabhagIdAndStatus(prabhagId, ComplaintStatus.RESOLVED);
        } else if (admin.getRole() == Role.SUPER_ADMIN) {
            List<ComplaintStatus> statuses = complaintRepository.findAllStatuses();
            total = statuses.size();
            pending = statuses.stream().filter(s -> s == ComplaintStatus.PENDING).count();
            inProgress = statuses.stream().filter(s -> s == ComplaintStatus.IN_PROGRESS).count();
            resolved = statuses.stream().filter(s -> s == ComplaintStatus.RESOLVED).count();
        } else {
            throw new AccessDeniedException("Unauthorized access");
        }

        return ComplaintStatsResponse.builder()
                .total(total)
                .pending(pending)
                .inProgress(inProgress)
                .resolved(resolved)
                .build();
    }

    /**
     * Add images to an existing complaint.
     *
     * @param complaintId complaint ID
     * @param userId      user ID (must be owner)
     * @param files       list of image files
     * @return list of image URLs/IDs
     */
    @Transactional
    public List<String> addImagesToComplaint(UUID complaintId, UUID userId,
            List<org.springframework.web.multipart.MultipartFile> files) {
        log.info("Adding images to complaint: {}", complaintId);

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", complaintId));

        // Validate owner
        if (!complaint.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You are not authorized to upload images for this complaint");
        }

        // Validate status
        if (complaint.getStatus() == ComplaintStatus.RESOLVED) {
            throw new IllegalStateException("Cannot add images to a resolved complaint");
        }

        List<ComplaintAttachment> attachments = complaintAttachmentService.uploadImages(complaint, files);

        return attachments.stream()
                .map(att -> baseUrl + "/api/complaints/attachments/" + att.getId())
                .toList();
    }

    /**
     * Get complaints for Super Admin with optional filters.
     *
     * @param adminId   optional admin ID filter
     * @param prabhagId optional prabhag ID filter
     * @param status    optional status filter
     * @param pageable  pagination information
     * @return paginated list of complaints
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ComplaintResponse> getComplaintsForSuperAdmin(UUID adminId, Integer prabhagId,
            ComplaintStatus status, Pageable pageable) {
        Page<Complaint> page;

        if (prabhagId != null) {
            // Filter by specific Prabhag ID
            if (status != null) {
                page = complaintRepository.findByUserPrabhagIdAndStatusOrderByCreatedAtDesc(prabhagId, status,
                        pageable);
            } else {
                page = complaintRepository.findByUserPrabhagIdOrderByCreatedAtDesc(prabhagId, pageable);
            }
        } else if (adminId != null) {
            // Filter by Admin's Prabhag
            User admin = userRepository.findById(adminId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

            if (admin.getRole() != Role.ADMIN) {
                throw new IllegalArgumentException("Provided user ID is not an ADMIN");
            }

            if (admin.getPrabhag() == null) {
                page = Page.empty(pageable);
            } else {
                if (status != null) {
                    page = complaintRepository.findByUserPrabhagIdAndStatusOrderByCreatedAtDesc(
                            admin.getPrabhag().getId(), status, pageable);
                } else {
                    page = complaintRepository.findByUserPrabhagIdOrderByCreatedAtDesc(admin.getPrabhag().getId(),
                            pageable);
                }
            }
        } else {
            // Global fetch
            if (status != null) {
                page = complaintRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
            } else {
                page = complaintRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        }

        List<ComplaintResponse> content = page.map(this::mapToResponse).getContent();

        return PaginatedResponse.<ComplaintResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    /**
     * Get detailed view of a complaint.
     * Enforces strict role-based access control.
     *
     * @param complaintId complaint ID
     * @param userId      requesting user ID
     * @return full complaint details
     */
    @Transactional(readOnly = true)
    public com.civiccomplaint.complaint.dto.ComplaintDetailResponse getComplaintDetails(UUID complaintId, UUID userId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", complaintId));

        User requestingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Role-based Access Control
        if (requestingUser.getRole() == Role.CITIZEN) {
            // Citizen can only view their own complaints
            if (!complaint.getUser().getId().equals(userId)) {
                throw new AccessDeniedException("You are not authorized to view this complaint");
            }
        } else if (requestingUser.getRole() == Role.ADMIN) {
            // Admin can only view complaints in their Prabhag
            if (requestingUser.getPrabhag() == null) {
                throw new AccessDeniedException("Admin is not assigned to any Prabhag");
            }
            if (complaint.getUser().getPrabhag() == null ||
                    !complaint.getUser().getPrabhag().getId().equals(requestingUser.getPrabhag().getId())) {
                throw new AccessDeniedException("You are not authorized to view complaints outside your Prabhag");
            }
        } else if (requestingUser.getRole() == Role.SUPER_ADMIN) {
            // Super Admin can view all
        } else {
            throw new AccessDeniedException("Unauthorized role");
        }

        // Map to Detail Response
        return mapToDetailResponse(complaint);
    }

    private com.civiccomplaint.complaint.dto.ComplaintDetailResponse mapToDetailResponse(Complaint complaint) {
        User citizen = complaint.getUser();
        com.civiccomplaint.master.Prabhag prabhag = citizen.getPrabhag();

        // Citizen Info
        var raisedBy = com.civiccomplaint.complaint.dto.ComplaintDetailResponse.CitizenInfo.builder()
                .id(citizen.getId())
                .fullName(citizen.getFullName())
                .mobileNumber(citizen.getMobileNumber())
                .email(citizen.getEmail())
                .address(citizen.getAddress())
                .pinCode(citizen.getPinCode())
                .prabhagName(prabhag != null ? prabhag.getName() : "N/A")
                .build();

        // Admin Info (Prabhag context)
        var adminInfo = com.civiccomplaint.complaint.dto.ComplaintDetailResponse.AdminInfo.builder()
                .prabhagId(prabhag != null ? prabhag.getId() : null)
                .prabhagName(prabhag != null ? prabhag.getName() : "Unassigned")
                .build();

        // Location Info
        var locationInfo = com.civiccomplaint.complaint.dto.ComplaintDetailResponse.LocationInfo.builder()
                .latitude(complaint.getLatitude())
                .longitude(complaint.getLongitude())
                .locationText(complaint.getLocationText())
                .build();

        // Attachments
        var attachmentInfos = complaint.getAttachments().stream()
                .map(att -> com.civiccomplaint.complaint.dto.ComplaintDetailResponse.AttachmentInfo.builder()
                        .id(att.getId())
                        .url(baseUrl + "/api/complaints/attachments/" + att.getId())
                        .contentType(att.getContentType())
                        .build())
                .toList();

        return com.civiccomplaint.complaint.dto.ComplaintDetailResponse.builder()
                .id(complaint.getId())
                .complaintNumber(complaint.getComplaintNumber())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .complaintType(complaint.getComplaintType())
                .status(complaint.getStatus())
                .createdAt(complaint.getCreatedAt())
                .raisedBy(raisedBy)
                .adminInfo(adminInfo)
                .location(locationInfo)
                .attachments(attachmentInfos)
                .build();
    }
}
