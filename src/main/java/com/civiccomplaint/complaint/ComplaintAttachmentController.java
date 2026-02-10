package com.civiccomplaint.complaint;

import com.civiccomplaint.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for serving complaint attachment images from database.
 */
@Slf4j
@RestController
@RequestMapping("/complaints/attachments")
@RequiredArgsConstructor
public class ComplaintAttachmentController {

        private final ComplaintAttachmentRepository attachmentRepository;
        private final ComplaintRepository complaintRepository;

        /**
         * Get image attachment by ID.
         * Citizens can only access their own complaint images.
         * Admins can access all images.
         *
         * @param attachmentId   the attachment ID
         * @param authentication Spring Security authentication
         * @return image data as byte array
         */
        @GetMapping("/{attachmentId}")
        @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<byte[]> getAttachment(
                        @PathVariable UUID attachmentId,
                        Authentication authentication) {

                log.info("GET /complaints/attachments/{} - User: {}", attachmentId, authentication.getName());

                ComplaintAttachment attachment = attachmentRepository.findById(attachmentId)
                                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));

                // Security check for citizens
                if (authentication.getAuthorities().stream()
                                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CITIZEN"))) {
                        com.civiccomplaint.auth.CustomUserDetails userDetails = (com.civiccomplaint.auth.CustomUserDetails) authentication
                                        .getPrincipal();
                        UUID userId = userDetails.getId();
                        Complaint complaint = attachment.getComplaint();

                        if (!complaint.getUser().getId().equals(userId)) {
                                throw new org.springframework.security.access.AccessDeniedException(
                                                "You are not authorized to access this attachment");
                        }
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(attachment.getContentType()));
                headers.setContentDispositionFormData("inline", attachment.getFileName());
                headers.setContentLength(attachment.getImageData().length);

                return ResponseEntity.ok()
                                .headers(headers)
                                .body(attachment.getImageData());
        }
}
