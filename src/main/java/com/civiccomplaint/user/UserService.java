package com.civiccomplaint.user;

import com.civiccomplaint.exception.ResourceNotFoundException;
import com.civiccomplaint.master.Prabhag;
import com.civiccomplaint.master.PrabhagRepository;
import com.civiccomplaint.master.CorporatorRepository;
import com.civiccomplaint.user.dto.CreateAdminRequest;
import com.civiccomplaint.master.dto.PrabhagResponse;
import com.civiccomplaint.user.dto.UpdateAdminRequest;
import com.civiccomplaint.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PrabhagRepository prabhagRepository;
    private final CorporatorRepository corporatorRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new Admin user.
     * Can only be called by SUPER_ADMIN (enforced by controller).
     *
     * @param request admin creation request
     * @return created user response
     */
    @Transactional
    public UserResponse createAdmin(CreateAdminRequest request) {
        log.info("Creating new Admin user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number already registered");
        }

        Prabhag prabhag = prabhagRepository.findById(request.getPrabhagId())
                .orElseThrow(() -> new ResourceNotFoundException("Prabhag", "id", request.getPrabhagId()));

        User admin = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .address(request.getAddress())
                .pinCode(request.getPinCode())
                .role(Role.ADMIN)
                .prabhag(prabhag) // Admin must map to a Prabhag
                .isActive(true)
                .build();

        admin = userRepository.save(admin);
        log.info("Admin created successfully with ID: {}", admin.getId());

        return mapToUserResponse(admin);
    }

    private UserResponse mapToUserResponse(User user) {
        PrabhagResponse prabhagResponse = null;
        if (user.getPrabhag() != null) {
            prabhagResponse = PrabhagResponse.builder()
                    .id(user.getPrabhag().getId())
                    .name(user.getPrabhag().getName())
                    .code(user.getPrabhag().getCode())
                    .description(user.getPrabhag().getDescription())
                    .build();
        }

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .address(user.getAddress())
                .pinCode(user.getPinCode())
                .hasPoster(user.getPosterImage() != null && user.getPosterImage().length > 0)
                .prabhag(prabhagResponse)
                .build();
    }

    /**
     * Upload a poster image for an admin.
     * Only admins can have poster images.
     *
     * @param adminId id of the admin user
     * @param file    image file
     * @return updated user response
     */
    @Transactional
    public void uploadAdminPoster(java.util.UUID adminId, org.springframework.web.multipart.MultipartFile file) {
        log.info("Uploading poster image for admin: {}", adminId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Poster images can only be uploaded for ADMIN users");
        }

        try {
            // Validate file size and type
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }
            if (file.getSize() > 2 * 1024 * 1024) { // 2MB
                throw new IllegalArgumentException("File size exceeds 2MB limit");
            }
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png")
                    && !contentType.equals("image/webp"))) {
                throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and WebP are allowed");
            }

            admin.setPosterImage(file.getBytes());
            admin.setPosterImageContentType(contentType);
            userRepository.save(admin);
            log.info("Poster image uploaded successfully for admin: {}", adminId);
        } catch (java.io.IOException e) {
            log.error("Failed to read poster image file", e);
            throw new RuntimeException("Failed to upload poster image");
        }
    }

    /**
     * Get poster image for a specific admin.
     *
     * @param adminId id of the admin
     * @return pair of image bytes and content type
     */
    @Transactional(readOnly = true)
    public org.springframework.data.util.Pair<byte[], String> getAdminPoster(java.util.UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Target user is not an ADMIN");
        }

        if (admin.getPosterImage() == null) {
            throw new ResourceNotFoundException("Poster", "admin", adminId);
        }

        return org.springframework.data.util.Pair.of(admin.getPosterImage(), admin.getPosterImageContentType());
    }

    /**
     * Get all admin users.
     *
     * @return list of admin users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllAdmins() {
        return userRepository.findByRole(Role.ADMIN).stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get admin poster image for a citizen.
     * Finds the admin associated with the citizen's prabhag.
     *
     * @param citizenId id of the citizen
     * @return pair of image bytes and content type
     */
    @Transactional(readOnly = true)
    public org.springframework.data.util.Pair<byte[], String> getAdminPosterForCitizen(java.util.UUID citizenId) {
        User citizen = userRepository.findById(citizenId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", citizenId));

        if (citizen.getPrabhag() == null) {
            throw new ResourceNotFoundException("Prabhag", "user", citizenId);
        }

        // Find an admin for this prabhag
        User admin = userRepository.findFirstByPrabhagIdAndRole(citizen.getPrabhag().getId(), Role.ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "prabhag", citizen.getPrabhag().getId()));

        if (admin.getPosterImage() == null) {
            throw new ResourceNotFoundException("Poster", "admin", admin.getId());
        }

        return org.springframework.data.util.Pair.of(admin.getPosterImage(), admin.getPosterImageContentType());
    }

    /**
     * Get admin poster image for a citizen by email.
     *
     * @param email email of the citizen
     * @return ResponseEntity with image
     */
    @Transactional(readOnly = true)
    public org.springframework.http.ResponseEntity<byte[]> getAdminPosterForCitizenByEmail(String email) {
        User citizen = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        org.springframework.data.util.Pair<byte[], String> posterData = getAdminPosterForCitizen(citizen.getId());

        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, posterData.getSecond())
                .body(posterData.getFirst());
    }

    /**
     * Create an Admin user from a Corporator.
     * Validates that the Corporator exists and has not already been used to create
     * a user.
     *
     * @param corporatorId ID of the corporator
     * @param password     raw password for the new admin account
     * @return created user response
     */
    @Transactional
    public UserResponse createAdminFromCorporator(Integer corporatorId,
            com.civiccomplaint.user.dto.CreateUserFromCorporatorRequest request) {
        log.info("Creating Admin user from Corporator ID: {}", corporatorId);

        com.civiccomplaint.master.Corporator corporator = corporatorRepository.findById(corporatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Corporator", "id", corporatorId));

        if (Boolean.TRUE.equals(corporator.getIsUserCreated())) {
            throw new IllegalArgumentException("User account already created for this Corporator");
        }

        // Determine Email and Mobile
        String emailToUse = (request.getEmail() != null && !request.getEmail().trim().isEmpty())
                ? request.getEmail()
                : corporator.getEmail();

        String mobileToUse = (request.getMobileNumber() != null && !request.getMobileNumber().trim().isEmpty())
                ? request.getMobileNumber()
                : corporator.getMobileNumber();

        // Validate presence
        if (emailToUse == null || emailToUse.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required. Please provide it in the request.");
        }
        if (mobileToUse == null || mobileToUse.trim().isEmpty()) {
            throw new IllegalArgumentException("Mobile number is required. Please provide it in the request.");
        }

        if (userRepository.existsByEmail(emailToUse)) {
            throw new IllegalArgumentException("Email already registered: " + emailToUse);
        }

        byte[] posterImageBytes = null;
        String posterImageContentType = null;

        // Handle poster image upload if present
        if (request.getPosterImage() != null && !request.getPosterImage().isEmpty()) {
            try {
                org.springframework.web.multipart.MultipartFile file = request.getPosterImage();
                if (file.getSize() > 2 * 1024 * 1024) { // 2MB
                    throw new IllegalArgumentException("File size exceeds 2MB limit");
                }
                String contentType = file.getContentType();
                if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png")
                        && !contentType.equals("image/webp"))) {
                    throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and WebP are allowed");
                }
                posterImageBytes = file.getBytes();
                posterImageContentType = contentType;
            } catch (java.io.IOException e) {
                log.error("Failed to process poster image", e);
                throw new RuntimeException("Failed to upload poster image", e);
            }
        }

        User admin = User.builder()
                .fullName(corporator.getFullName())
                .email(emailToUse)
                .mobileNumber(mobileToUse)
                .password(passwordEncoder.encode(request.getPassword()))
                .address(request.getAddress())
                .pinCode(request.getPinCode())
                .role(Role.ADMIN)
                .prabhag(corporator.getPrabhag())
                .isActive(true)
                .posterImage(posterImageBytes)
                .posterImageContentType(posterImageContentType)
                .build();

        admin = userRepository.save(admin);
        log.info("Admin created successfully for Corporator: {}", corporator.getFullName());

        corporator.setIsUserCreated(true);
        // Also update corporator email/mobile if provided and different (optional, but
        // good for consistency)
        if (request.getEmail() != null && !request.getEmail().equals(corporator.getEmail())) {
            corporator.setEmail(emailToUse);
        }
        if (request.getMobileNumber() != null && !request.getMobileNumber().equals(corporator.getMobileNumber())) {
            corporator.setMobileNumber(mobileToUse);
        }
        corporatorRepository.save(corporator);

        return mapToUserResponse(admin);
    }

    /**
     * Update an existing Admin user.
     * Can only be called by SUPER_ADMIN.
     *
     * @param adminId id of the admin user
     * @param request update request
     * @return updated user response
     */
    @Transactional
    public UserResponse updateAdmin(java.util.UUID adminId, UpdateAdminRequest request) {
        log.info("Updating Admin user: {}", adminId);

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only ADMIN users can be updated via this endpoint");
        }

        // Validate uniqueness if changed
        if (!admin.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (!admin.getMobileNumber().equals(request.getMobileNumber())
                && userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number already registered");
        }

        Prabhag prabhag = prabhagRepository.findById(request.getPrabhagId())
                .orElseThrow(() -> new ResourceNotFoundException("Prabhag", "id", request.getPrabhagId()));

        admin.setFullName(request.getFullName());
        admin.setEmail(request.getEmail());
        admin.setMobileNumber(request.getMobileNumber());
        admin.setAddress(request.getAddress());
        admin.setPinCode(request.getPinCode());
        admin.setPrabhag(prabhag);

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        admin = userRepository.save(admin);
        log.info("Admin updated successfully with ID: {}", admin.getId());

        return mapToUserResponse(admin);
    }
}
