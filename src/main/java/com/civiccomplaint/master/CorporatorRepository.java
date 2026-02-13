package com.civiccomplaint.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Corporator entity.
 */
@Repository
public interface CorporatorRepository extends JpaRepository<Corporator, Integer> {

    /**
     * Find all corporators ordered by creation date descending.
     *
     * @return list of corporators
     */
    List<Corporator> findAllByOrderByCreatedAtDesc();

}
