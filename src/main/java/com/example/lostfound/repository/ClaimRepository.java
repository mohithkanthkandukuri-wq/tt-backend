package com.example.lostfound.repository;

import com.example.lostfound.entity.Claim;
import com.example.lostfound.entity.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByItemId(Long itemId);

    Page<Claim> findByClaimantId(Long claimantId, Pageable pageable);

    boolean existsByItemIdAndClaimantIdAndStatusIn(Long itemId, Long claimantId, Collection<ClaimStatus> statuses);

    long countByStatus(ClaimStatus status);

    Page<Claim> findAll(Pageable pageable);
}
