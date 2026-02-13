package com.civiccomplaint.complaint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service for handling complaint image attachments stored in database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintAttachmentService {

    private final ComplaintAttachmentRepository attachmentRepository;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/jpg");

    /**
     * Upload and save images to database for a complaint.
     *
     * @param complaint the complaint entity
     * @param files     list of image files
     * @return list of saved attachments
     */
    @Transactional
    public List<ComplaintAttachment> uploadImages(Complaint complaint, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<ComplaintAttachment> attachments = new ArrayList<>();

        for (MultipartFile file : files) {
            validateFile(file);

            try {
                ComplaintAttachment attachment = ComplaintAttachment.builder()
                        .complaint(complaint)
                        .imageData(file.getBytes())
                        .contentType(file.getContentType())
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .build();

                ComplaintAttachment saved = attachmentRepository.save(attachment);
                attachments.add(saved);
                complaint.addAttachment(saved);

                log.info("Saved image attachment: {} for complaint: {}", saved.getId(), complaint.getId());
            } catch (IOException e) {
                log.error("Failed to read file: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("Failed to process image file: " + file.getOriginalFilename(), e);
            }
        }

        return attachments;
    }

    /**
     * Validate uploaded file.
     *
     * @param file the file to validate
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 2MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and WebP images are allowed");
        }
    }
}
