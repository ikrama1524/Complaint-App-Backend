package com.civiccomplaint.user;

import com.civiccomplaint.exception.ResourceNotFoundException;
import com.civiccomplaint.master.Prabhag;
import com.civiccomplaint.master.PrabhagRepository;
import com.civiccomplaint.user.dto.CreateAdminRequest;
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
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
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
     * Get all admin users.
     *
     * @return list of admin users
     */
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
}
