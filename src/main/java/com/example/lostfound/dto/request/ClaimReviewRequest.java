package com.example.lostfound.dto.request;

import jakarta.validation.constraints.Size;

public class ClaimReviewRequest {

    @Size(max = 1000, message = "Review message cannot exceed 1000 characters")
    private String reviewMessage;

    public ClaimReviewRequest() {}

    public ClaimReviewRequest(String reviewMessage) {
        this.reviewMessage = reviewMessage;
    }

    public String getReviewMessage() {
        return reviewMessage;
    }

    public void setReviewMessage(String reviewMessage) {
        this.reviewMessage = reviewMessage;
    }
}
