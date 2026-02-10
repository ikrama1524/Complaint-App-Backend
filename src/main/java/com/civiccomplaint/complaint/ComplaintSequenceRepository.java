package com.civiccomplaint.complaint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ComplaintSequenceRepository extends JpaRepository<Complaint, UUID> {

    @Query(value = """
            INSERT INTO complaint_sequences (prabhag_id, year, current_value)
            VALUES (:prabhagId, :year, 1)
            ON CONFLICT (prabhag_id, year)
            DO UPDATE SET current_value = complaint_sequences.current_value + 1
            RETURNING current_value
            """, nativeQuery = true)
    int getNextSequenceValue(@Param("prabhagId") UUID prabhagId, @Param("year") int year);
}
