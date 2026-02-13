package com.civiccomplaint.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email address.
     *
     * @param email the email address
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by mobile number.
     *
     * @param mobileNumber the mobile number
     * @return Optional containing user if found
     */
    Optional<User> findByMobileNumber(String mobileNumber);

    /**
     * Check if email already exists.
     *
     * @param email the email address
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if mobile number already exists.
     *
     * @param mobileNumber the mobile number
     * @return true if mobile number exists
     */
    boolean existsByMobileNumber(String mobileNumber);

    /**
     * Find active user by email.
     *
     * @param email the email address
     * @return Optional containing active user if found
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * Find active user by mobile number.
     *
     * @param mobileNumber the mobile number
     * @return Optional containing active user if found
     */
    Optional<User> findByMobileNumberAndIsActiveTrue(String mobileNumber);

    /**
     * Find users by role.
     *
     * @param role user role
     * @return list of users
     */
    /**
     * Find users by role.
     *
     * @param role user role
     * @return list of users
     */
    java.util.List<User> findByRole(Role role);

    /**
     * Find first user by prabhag ID and role.
     *
     * @param prabhagId prabhag ID
     * @param role      user role
     * @return Optional containing user if found
     */
    Optional<User> findFirstByPrabhagIdAndRole(Integer prabhagId, Role role);
}
