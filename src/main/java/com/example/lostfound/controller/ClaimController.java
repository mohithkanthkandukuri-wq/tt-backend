package com.example.lostfound.controller;

import com.example.lostfound.dto.request.ClaimCreateRequest;
import com.example.lostfound.dto.request.ClaimReviewRequest;
import com.example.lostfound.dto.response.ClaimResponse;
import com.example.lostfound.dto.response.ItemResponse;
import com.example.lostfound.dto.response.PagedResponse;
import com.example.lostfound.entity.Role;
import com.example.lostfound.security.UserPrincipal;
import com.example.lostfound.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping("/api/items/{id}/claims")
    public ResponseEntity<ClaimResponse> createClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ClaimCreateRequest request) {
        ClaimResponse response = claimService.createClaim(id, request, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/claims/my")
    public ResponseEntity<PagedResponse<ClaimResponse>> getMyClaims(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        PagedResponse<ClaimResponse> response = claimService.getMyClaims(currentUser.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/claims/{id}")
    public ResponseEntity<ClaimResponse> getClaimById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        ClaimResponse response = claimService.getClaimById(id, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/items/{id}/claims")
    public ResponseEntity<List<ClaimResponse>> getItemClaims(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        List<ClaimResponse> response = claimService.getItemClaims(id, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/claims/{id}/approve")
    public ResponseEntity<ClaimResponse> approveClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody(required = false) ClaimReviewRequest request) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        ClaimResponse response = claimService.approveClaim(id, request, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/claims/{id}/reject")
    public ResponseEntity<ClaimResponse> rejectClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody(required = false) ClaimReviewRequest request) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        ClaimResponse response = claimService.rejectClaim(id, request, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/claims/{id}/cancel")
    public ResponseEntity<ClaimResponse> cancelClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ClaimResponse response = claimService.cancelClaim(id, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/items/{id}/return")
    public ResponseEntity<ItemResponse> markItemReturned(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        ItemResponse response = claimService.markItemReturned(id, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(response);
    }
}
