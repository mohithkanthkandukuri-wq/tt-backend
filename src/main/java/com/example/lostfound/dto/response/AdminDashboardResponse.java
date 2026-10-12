package com.example.lostfound.dto.response;

import java.util.List;

public class AdminDashboardResponse {
    private long totalUsers;
    private long totalLostReports;
    private long totalFoundReports;
    private long openReports;
    private long returnedItems;
    private long pendingClaims;
    private List<ItemResponse> recentReports;
    private List<ClaimResponse> recentClaims;

    public AdminDashboardResponse() {}

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalLostReports() {
        return totalLostReports;
    }

    public void setTotalLostReports(long totalLostReports) {
        this.totalLostReports = totalLostReports;
    }

    public long getTotalFoundReports() {
        return totalFoundReports;
    }

    public void setTotalFoundReports(long totalFoundReports) {
        this.totalFoundReports = totalFoundReports;
    }

    public long getOpenReports() {
        return openReports;
    }

    public void setOpenReports(long openReports) {
        this.openReports = openReports;
    }

    public long getReturnedItems() {
        return returnedItems;
    }

    public void setReturnedItems(long returnedItems) {
        this.returnedItems = returnedItems;
    }

    public long getPendingClaims() {
        return pendingClaims;
    }

    public void setPendingClaims(long pendingClaims) {
        this.pendingClaims = pendingClaims;
    }

    public List<ItemResponse> getRecentReports() {
        return recentReports;
    }

    public void setRecentReports(List<ItemResponse> recentReports) {
        this.recentReports = recentReports;
    }

    public List<ClaimResponse> getRecentClaims() {
        return recentClaims;
    }

    public void setRecentClaims(List<ClaimResponse> recentClaims) {
        this.recentClaims = recentClaims;
    }
}
