package com.example.lostfound.dto.response;

import com.example.lostfound.entity.Item;
import com.example.lostfound.entity.ItemCategory;
import com.example.lostfound.entity.ItemStatus;
import com.example.lostfound.entity.ReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItemResponse {
    private Long id;
    private String title;
    private String description;
    private ItemCategory category;
    private ReportType reportType;
    private String location;
    private LocalDate incidentDate;
    private ItemStatus status;
    private String imagePath;
    private Long reporterId;
    private String reporterName;
    private String reporterEmail;
    private String reporterPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int claimCount;

    public ItemResponse() {}

    public ItemResponse(Item item) {
        if (item != null) {
            this.id = item.getId();
            this.title = item.getTitle();
            this.description = item.getDescription();
            this.category = item.getCategory();
            this.reportType = item.getReportType();
            this.location = item.getLocation();
            this.incidentDate = item.getIncidentDate();
            this.status = item.getStatus();
            this.imagePath = item.getImagePath();
            if (item.getReporter() != null) {
                this.reporterId = item.getReporter().getId();
                this.reporterName = item.getReporter().getName();
                this.reporterEmail = item.getReporter().getEmail();
                this.reporterPhone = item.getReporter().getPhone();
            }
            this.createdAt = item.getCreatedAt();
            this.updatedAt = item.getUpdatedAt();
        }
    }

    public ItemResponse(Item item, int claimCount) {
        this(item);
        this.claimCount = claimCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
    }

    public String getReporterPhone() {
        return reporterPhone;
    }

    public void setReporterPhone(String reporterPhone) {
        this.reporterPhone = reporterPhone;
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

    public int getClaimCount() {
        return claimCount;
    }

    public void setClaimCount(int claimCount) {
        this.claimCount = claimCount;
    }
}
