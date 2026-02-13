package com.civiccomplaint.master;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Prabhag entity.
 */
@Repository
public interface PrabhagRepository extends JpaRepository<Prabhag, Integer> {

    boolean existsByName(String name);

    boolean existsByCode(String code);

    Optional<Prabhag> findByName(String name);
}
