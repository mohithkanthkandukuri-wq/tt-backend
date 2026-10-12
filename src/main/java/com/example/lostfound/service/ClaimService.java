package com.example.lostfound.service;

import com.example.lostfound.dto.request.ClaimCreateRequest;
import com.example.lostfound.dto.request.ClaimReviewRequest;
import com.example.lostfound.dto.response.ClaimResponse;
import com.example.lostfound.dto.response.ItemResponse;
import com.example.lostfound.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClaimService {
    ClaimResponse createClaim(Long itemId, ClaimCreateRequest request, Long claimantId);
    PagedResponse<ClaimResponse> getMyClaims(Long claimantId, Pageable pageable);
    ClaimResponse getClaimById(Long claimId, Long userId, boolean isAdmin);
    List<ClaimResponse> getItemClaims(Long itemId, Long userId, boolean isAdmin);
    ClaimResponse approveClaim(Long claimId, ClaimReviewRequest request, Long userId, boolean isAdmin);
    ClaimResponse rejectClaim(Long claimId, ClaimReviewRequest request, Long userId, boolean isAdmin);
    ClaimResponse cancelClaim(Long claimId, Long claimantId);
    ItemResponse markItemReturned(Long itemId, Long userId, boolean isAdmin);
}
