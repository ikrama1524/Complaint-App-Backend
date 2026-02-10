package com.civiccomplaint.auth;

import com.civiccomplaint.auth.dto.AuthResponse;
import com.civiccomplaint.auth.dto.LoginRequest;
import com.civiccomplaint.auth.dto.RegisterRequest;
import com.civiccomplaint.exception.ResourceNotFoundException;
import com.civiccomplaint.user.Role;
import com.civiccomplaint.user.User;
import com.civiccomplaint.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations.
 * Handles registration, login, and token generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final com.civiccomplaint.master.PrabhagRepository prabhagRepository; // Inject PrabhagRepository
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Register a new citizen.
     *
     * @param request registration request
     * @return authentication response with token
     */
    @Transactional
    public AuthResponse registerCitizen(RegisterRequest request) {
        log.info("Registering new citizen with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Check if mobile number already exists
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number already registered");
        }

        // Validate and fetch Prabhag
        com.civiccomplaint.master.Prabhag prabhag = prabhagRepository.findById(request.getPrabhagId())
                .orElseThrow(() -> new ResourceNotFoundException("Prabhag", "id", request.getPrabhagId()));

        // Create new user
        User user = User.builder()
                .role(Role.CITIZEN)
                .fullName(request.getFullName())
                .mobileNumber(request.getMobileNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .pinCode(request.getPinCode())
                .password(passwordEncoder.encode(request.getPassword()))
                .prabhag(prabhag) // Assign Prabhag
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("Citizen registered successfully with ID: {}", user.getId());

        // Generate token
        String token = jwtUtil.generateToken(user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Login for citizen.
     *
     * @param request login request
     * @return authentication response with token
     */
    @Transactional(readOnly = true)
    public AuthResponse loginCitizen(LoginRequest request) {
        log.info("Citizen login attempt with identifier: {}", request.getIdentifier());
        return login(request, Role.CITIZEN);
    }

    /**
     * Login for admin.
     *
     * @param request login request
     * @return authentication response with token
     */
    /**
     * Login for admin.
     *
     * @param request login request
     * @return authentication response with token
     */
    @Transactional(readOnly = true)
    public AuthResponse loginAdmin(LoginRequest request) {
        log.info("Admin login attempt with identifier: {}", request.getIdentifier());
        // Allow both ADMIN and SUPER_ADMIN to use admin login
        return login(request, Role.ADMIN, Role.SUPER_ADMIN);
    }

    /**
     * Common login logic for both citizen and admin.
     *
     * @param request      login request
     * @param allowedRoles allowed user roles
     * @return authentication response with token
     */
    private AuthResponse login(LoginRequest request, Role... allowedRoles) {
        // Find user by email or mobile number
        User user = findUserByIdentifier(request.getIdentifier());

        // Verify user role
        boolean isRoleAllowed = java.util.Arrays.asList(allowedRoles).contains(user.getRole());
        if (!isRoleAllowed) {
            log.warn("Role mismatch for user: {}. Expected one of: {}, Actual: {}",
                    user.getId(), java.util.Arrays.toString(allowedRoles), user.getRole());
            throw new BadCredentialsException("Invalid credentials");
        }

        // Verify user is active
        if (!user.getIsActive()) {
            log.warn("Inactive user login attempt: {}", user.getId());
            throw new BadCredentialsException("Account is inactive");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for user: {}", user.getId());
            throw new BadCredentialsException("Invalid credentials");
        }

        log.info("User logged in successfully: {} with role: {}", user.getId(), user.getRole());

        // Generate token
        String token = jwtUtil.generateToken(user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Find user by email or mobile number.
     *
     * @param identifier email or mobile number
     * @return user
     */
    private User findUserByIdentifier(String identifier) {
        // Try to find by email first
        if (identifier.contains("@")) {
            return userRepository.findByEmailAndIsActiveTrue(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + identifier));
        }

        // Otherwise, find by mobile number
        return userRepository.findByMobileNumberAndIsActiveTrue(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile number: " + identifier));
    }
}
