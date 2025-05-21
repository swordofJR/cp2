package com.copyright.repository;

import com.copyright.entity.Copyright;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CopyrightRepository extends JpaRepository<Copyright, Long> {
    List<Copyright> findByOwnerAddress(String ownerAddress);

    List<Copyright> findByStatus(String status);

    List<Copyright> findByStatusAndOwnerAddress(String status, String ownerAddress);
}