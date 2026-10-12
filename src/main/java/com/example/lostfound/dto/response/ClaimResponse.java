package com.example.lostfound.dto.response;

import com.example.lostfound.entity.Claim;
import com.example.lostfound.entity.ClaimStatus;
import com.example.lostfound.entity.ItemCategory;
import com.example.lostfound.entity.ReportType;

import java.time.LocalDateTime;

public class ClaimResponse {
    private Long id;
    private Long itemId;
    private String itemTitle;
    private ItemCategory itemCategory;
    private ReportType itemReportType;
    private Long claimantId;
    private String claimantName;
    private String claimantEmail;
    private String claimantPhone;
    private String message;
    private ClaimStatus status;
    private Long reviewedById;
    private String reviewedByName;
    private String reviewMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ClaimResponse() {}

    public ClaimResponse(Claim claim) {
        if (claim != null) {
            this.id = claim.getId();
            if (claim.getItem() != null) {
                this.itemId = claim.getItem().getId();
                this.itemTitle = claim.getItem().getTitle();
                this.itemCategory = claim.getItem().getCategory();
                this.itemReportType = claim.getItem().getReportType();
            }
            if (claim.getClaimant() != null) {
                this.claimantId = claim.getClaimant().getId();
                this.claimantName = claim.getClaimant().getName();
                this.claimantEmail = claim.getClaimant().getEmail();
                this.claimantPhone = claim.getClaimant().getPhone();
            }
            this.message = claim.getMessage();
            this.status = claim.getStatus();
            if (claim.getReviewedBy() != null) {
                this.reviewedById = claim.getReviewedBy().getId();
                this.reviewedByName = claim.getReviewedBy().getName();
            }
            this.reviewMessage = claim.getReviewMessage();
            this.createdAt = claim.getCreatedAt();
            this.updatedAt = claim.getUpdatedAt();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public ItemCategory getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(ItemCategory itemCategory) {
        this.itemCategory = itemCategory;
    }

    public ReportType getItemReportType() {
        return itemReportType;
    }

    public void setItemReportType(ReportType itemReportType) {
        this.itemReportType = itemReportType;
    }

    public Long getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(Long claimantId) {
        this.claimantId = claimantId;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public void setClaimantName(String claimantName) {
        this.claimantName = claimantName;
    }

    public String getClaimantEmail() {
        return claimantEmail;
    }

    public void setClaimantEmail(String claimantEmail) {
        this.claimantEmail = claimantEmail;
    }

    public String getClaimantPhone() {
        return claimantPhone;
    }

    public void setClaimantPhone(String claimantPhone) {
        this.claimantPhone = claimantPhone;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public Long getReviewedById() {
        return reviewedById;
    }

    public void setReviewedById(Long reviewedById) {
        this.reviewedById = reviewedById;
    }

    public String getReviewedByName() {
        return reviewedByName;
    }

    public void setReviewedByName(String reviewedByName) {
        this.reviewedByName = reviewedByName;
    }

    public String getReviewMessage() {
        return reviewMessage;
    }

    public void setReviewMessage(String reviewMessage) {
        this.reviewMessage = reviewMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
