package com.civiccomplaint.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Prabhag entity.
 */
@Repository
public interface PrabhagRepository extends JpaRepository<Prabhag, UUID> {

    boolean existsByName(String name);

    boolean existsByCode(String code);

    Optional<Prabhag> findByName(String name);
}
