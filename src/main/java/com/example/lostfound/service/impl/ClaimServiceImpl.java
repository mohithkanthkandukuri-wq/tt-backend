package com.example.lostfound.service.impl;

import com.example.lostfound.dto.request.ClaimCreateRequest;
import com.example.lostfound.dto.request.ClaimReviewRequest;
import com.example.lostfound.dto.response.ClaimResponse;
import com.example.lostfound.dto.response.ItemResponse;
import com.example.lostfound.dto.response.PagedResponse;
import com.example.lostfound.entity.Claim;
import com.example.lostfound.entity.ClaimStatus;
import com.example.lostfound.entity.Item;
import com.example.lostfound.entity.ItemStatus;
import com.example.lostfound.entity.ReportType;
import com.example.lostfound.entity.User;
import com.example.lostfound.exception.BadRequestException;
import com.example.lostfound.exception.ForbiddenException;
import com.example.lostfound.exception.ResourceNotFoundException;
import com.example.lostfound.repository.ClaimRepository;
import com.example.lostfound.repository.ItemRepository;
import com.example.lostfound.service.ClaimService;
import com.example.lostfound.service.NotificationService;
import com.example.lostfound.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public ClaimServiceImpl(ClaimRepository claimRepository,
                            ItemRepository itemRepository,
                            UserService userService,
                            NotificationService notificationService) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public ClaimResponse createClaim(Long itemId, ClaimCreateRequest request, Long claimantId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (item.getReportType() != ReportType.FOUND) {
            throw new BadRequestException("Claims can only be filed against items reported as FOUND");
        }

        if (item.getStatus() == ItemStatus.CLAIMED || item.getStatus() == ItemStatus.RETURNED || item.getStatus() == ItemStatus.CLOSED) {
            throw new BadRequestException("This item is no longer available for claims (Status: " + item.getStatus() + ")");
        }

        if (item.getReporter().getId().equals(claimantId)) {
            throw new BadRequestException("You cannot submit a claim for an item you reported yourself");
        }

        boolean alreadyPending = claimRepository.existsByItemIdAndClaimantIdAndStatusIn(
                itemId, claimantId, Collections.singletonList(ClaimStatus.PENDING)
        );
        if (alreadyPending) {
            throw new BadRequestException("You already have a pending claim for this item");
        }

        User claimant = userService.getUserById(claimantId);
        Claim claim = new Claim(item, claimant, request.getMessage().trim());
        Claim savedClaim = claimRepository.save(claim);

        // Update item status to CLAIM_PENDING if currently OPEN
        if (item.getStatus() == ItemStatus.OPEN) {
            item.setStatus(ItemStatus.CLAIM_PENDING);
            itemRepository.save(item);
        }

        // Notify item reporter
        notificationService.createNotification(
                item.getReporter(),
                "New Claim Submitted",
                claimant.getName() + " submitted a claim for your found item: \"" + item.getTitle() + "\""
        );

        return new ClaimResponse(savedClaim);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ClaimResponse> getMyClaims(Long claimantId, Pageable pageable) {
        Page<Claim> claims = claimRepository.findByClaimantId(claimantId, pageable);
        List<ClaimResponse> content = claims.getContent().stream()
                .map(ClaimResponse::new)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                claims.getNumber(),
                claims.getSize(),
                claims.getTotalElements(),
                claims.getTotalPages(),
                claims.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(Long claimId, Long userId, boolean isAdmin) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        boolean isClaimant = claim.getClaimant().getId().equals(userId);
        boolean isReporter = claim.getItem().getReporter().getId().equals(userId);

        if (!isClaimant && !isReporter && !isAdmin) {
            throw new ForbiddenException("You do not have permission to view this claim");
        }

        return new ClaimResponse(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponse> getItemClaims(Long itemId, Long userId, boolean isAdmin) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (!item.getReporter().getId().equals(userId) && !isAdmin) {
            throw new ForbiddenException("You do not have permission to view claims for this item");
        }

        return claimRepository.findByItemId(itemId).stream()
                .map(ClaimResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ClaimResponse approveClaim(Long claimId, ClaimReviewRequest request, Long userId, boolean isAdmin) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        Item item = claim.getItem();
        if (!item.getReporter().getId().equals(userId) && !isAdmin) {
            throw new ForbiddenException("Only the reporter or an administrator can approve this claim");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("Only pending claims can be approved");
        }

        if (item.getStatus() == ItemStatus.CLAIMED || item.getStatus() == ItemStatus.RETURNED) {
            throw new BadRequestException("This item has already been marked as claimed or returned");
        }

        User reviewer = userService.getUserById(userId);
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setReviewedBy(reviewer);
        if (request != null && request.getReviewMessage() != null) {
            claim.setReviewMessage(request.getReviewMessage().trim());
        }
        Claim savedClaim = claimRepository.save(claim);

        // Update item status
        item.setStatus(ItemStatus.CLAIMED);
        itemRepository.save(item);

        // Reject other pending claims for this item
        List<Claim> otherClaims = claimRepository.findByItemId(item.getId());
        for (Claim other : otherClaims) {
            if (!other.getId().equals(claimId) && other.getStatus() == ClaimStatus.PENDING) {
                other.setStatus(ClaimStatus.REJECTED);
                other.setReviewedBy(reviewer);
                other.setReviewMessage("Another claim was approved for this item.");
                claimRepository.save(other);

                notificationService.createNotification(
                        other.getClaimant(),
                        "Claim Status Update",
                        "Another claimant was verified for the item: \"" + item.getTitle() + "\"."
                );
            }
        }

        // Notify approved claimant
        notificationService.createNotification(
                claim.getClaimant(),
                "Claim Approved!",
                "Congratulations! Your claim for \"" + item.getTitle() + "\" was approved. Please check details to coordinate recovery."
        );

        return new ClaimResponse(savedClaim);
    }

    @Override
    @Transactional
    public ClaimResponse rejectClaim(Long claimId, ClaimReviewRequest request, Long userId, boolean isAdmin) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        Item item = claim.getItem();
        if (!item.getReporter().getId().equals(userId) && !isAdmin) {
            throw new ForbiddenException("Only the reporter or an administrator can review this claim");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("Only pending claims can be rejected");
        }

        User reviewer = userService.getUserById(userId);
        claim.setStatus(ClaimStatus.REJECTED);
        claim.setReviewedBy(reviewer);
        if (request != null && request.getReviewMessage() != null) {
            claim.setReviewMessage(request.getReviewMessage().trim());
        }
        Claim savedClaim = claimRepository.save(claim);

        // Check if there are other pending claims
        List<Claim> allItemClaims = claimRepository.findByItemId(item.getId());
        boolean hasPending = allItemClaims.stream().anyMatch(c -> c.getStatus() == ClaimStatus.PENDING);
        if (!hasPending && item.getStatus() == ItemStatus.CLAIM_PENDING) {
            item.setStatus(ItemStatus.OPEN);
            itemRepository.save(item);
        }

        // Notify claimant
        String msg = (request != null && request.getReviewMessage() != null && !request.getReviewMessage().isBlank())
                ? "Note: " + request.getReviewMessage()
                : "The identifying details provided did not match the item.";
        notificationService.createNotification(
                claim.getClaimant(),
                "Claim Rejected",
                "Your claim for \"" + item.getTitle() + "\" was rejected. " + msg
        );

        return new ClaimResponse(savedClaim);
    }

    @Override
    @Transactional
    public ClaimResponse cancelClaim(Long claimId, Long claimantId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        if (!claim.getClaimant().getId().equals(claimantId)) {
            throw new ForbiddenException("You can only cancel your own claims");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("Only pending claims can be cancelled");
        }

        claim.setStatus(ClaimStatus.CANCELLED);
        Claim savedClaim = claimRepository.save(claim);

        Item item = claim.getItem();
        List<Claim> allItemClaims = claimRepository.findByItemId(item.getId());
        boolean hasPending = allItemClaims.stream().anyMatch(c -> c.getStatus() == ClaimStatus.PENDING);
        if (!hasPending && item.getStatus() == ItemStatus.CLAIM_PENDING) {
            item.setStatus(ItemStatus.OPEN);
            itemRepository.save(item);
        }

        notificationService.createNotification(
                item.getReporter(),
                "Claim Cancelled",
                claim.getClaimant().getName() + " cancelled their claim for: \"" + item.getTitle() + "\"."
        );

        return new ClaimResponse(savedClaim);
    }

    @Override
    @Transactional
    public ItemResponse markItemReturned(Long itemId, Long userId, boolean isAdmin) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (!item.getReporter().getId().equals(userId) && !isAdmin) {
            throw new ForbiddenException("Only the reporter or an administrator can mark an item as returned");
        }

        if (item.getStatus() == ItemStatus.RETURNED) {
            throw new BadRequestException("Item is already marked as RETURNED");
        }

        item.setStatus(ItemStatus.RETURNED);
        Item updatedItem = itemRepository.save(item);

        // Notify approved claimant if one exists
        List<Claim> itemClaims = claimRepository.findByItemId(itemId);
        itemClaims.stream()
                .filter(c -> c.getStatus() == ClaimStatus.APPROVED)
                .findFirst()
                .ifPresent(approvedClaim -> {
                    notificationService.createNotification(
                            approvedClaim.getClaimant(),
                            "Item Marked as Returned",
                            "The item \"" + item.getTitle() + "\" has been officially marked as returned. Thank you for using FindIt+!"
                    );
                });

        return new ItemResponse(updatedItem, itemClaims.size());
    }
}
