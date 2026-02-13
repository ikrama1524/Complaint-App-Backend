package com.civiccomplaint.complaint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Complaint entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    /**
     * Find all complaints by user ID.
     *
     * @param userId the user ID
     * @return list of complaints
     */
    List<Complaint> findByUserId(UUID userId);

    /**
     * Find all complaints by user ID ordered by creation date descending.
     *
     * @param userId   the user ID
     * @param pageable pagination information
     * @return page of complaints
     */
    Page<Complaint> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find all complaints by status.
     *
     * @param status the complaint status
     * @return list of complaints
     */
    List<Complaint> findByStatus(ComplaintStatus status);

    /**
     * Find all complaints by status ordered by creation date descending.
     *
     * @param status the complaint status
     * @return list of complaints
     */
    /**
     * Find all complaints by status ordered by creation date descending.
     *
     * @param status   the complaint status
     * @param pageable pagination information
     * @return page of complaints
     */
    Page<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status, Pageable pageable);

    /**
     * Find all complaints by complaint type.
     *
     * @param type the complaint type
     * @return list of complaints
     */
    List<Complaint> findByComplaintType(ComplaintType type);

    /**
     * Find all complaints by user ID and status.
     *
     * @param userId the user ID
     * @param status the complaint status
     * @return list of complaints
     */
    List<Complaint> findByUserIdAndStatus(UUID userId, ComplaintStatus status);

    /**
     * Find all complaints by user ID and status ordered by creation date
     * descending.
     *
     * @param userId   the user ID
     * @param status   the complaint status
     * @param pageable pagination information
     * @return page of complaints
     */
    Page<Complaint> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, ComplaintStatus status, Pageable pageable);

    /**
     * Find all complaints ordered by creation date descending.
     *
     * @param pageable pagination information
     * @return page of all complaints
     */
    Page<Complaint> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Count complaints by status.
     *
     * @param status the complaint status
     * @return count of complaints
     */
    long countByStatus(ComplaintStatus status);

    /**
     * Count complaints by user ID.
     *
     * @param userId the user ID
     * @return count of complaints
     */
    long countByUserId(UUID userId);

    /**
     * Find complaints with attachments count.
     * Uses JOIN FETCH to avoid N+1 query problem.
     *
     * @return list of complaints with attachments loaded
     */
    @Query("SELECT DISTINCT c FROM Complaint c LEFT JOIN FETCH c.attachments ORDER BY c.createdAt DESC")
    List<Complaint> findAllWithAttachments();

    /**
     * Find complaint by ID with images.
     *
     * @param id the complaint ID
     * @return complaint with images loaded
     */
    @Query("SELECT c FROM Complaint c LEFT JOIN FETCH c.attachments WHERE c.id = :id")
    Complaint findByIdWithAttachments(@Param("id") UUID id);

    /**
     * Find all complaint statuses.
     * Used for statistics calculation to avoid enum parameter binding issues.
     *
     * @return list of all complaint statuses
     */
    @Query("SELECT c.status FROM Complaint c")
    List<ComplaintStatus> findAllStatuses();

    /**
     * Find all complaints by user's prabhag ID ordered by creation date descending.
     *
     * @param prabhagId the prabhag ID
     * @param pageable  pagination information
     * @return page of complaints
     */
    Page<Complaint> findByUserPrabhagIdOrderByCreatedAtDesc(Integer prabhagId, Pageable pageable);

    /**
     * Count complaints by user's prabhag ID and status.
     *
     * @param prabhagId the prabhag ID
     * @param status    the complaint status
     * @return count of complaints
     */
    long countByUserPrabhagIdAndStatus(Integer prabhagId, ComplaintStatus status);

    /**
     * Count complaints by user's prabhag ID.
     *
     * @param prabhagId the prabhag ID
     * @return count of complaints
     */
    long countByUserPrabhagId(Integer prabhagId);

    /**
     * Find complaints by user's prabhag ID and status with pagination.
     *
     * @param prabhagId the prabhag ID
     * @param status    the complaint status
     * @param pageable  pagination information
     * @return page of complaints
     */
    Page<Complaint> findByUserPrabhagIdAndStatusOrderByCreatedAtDesc(Integer prabhagId, ComplaintStatus status,
            Pageable pageable);
}
