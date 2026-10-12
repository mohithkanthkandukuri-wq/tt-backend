package com.example.lostfound.service;

import com.example.lostfound.dto.request.UpdateRoleRequest;
import com.example.lostfound.dto.response.AdminDashboardResponse;
import com.example.lostfound.dto.response.ClaimResponse;
import com.example.lostfound.dto.response.ItemResponse;
import com.example.lostfound.dto.response.PagedResponse;
import com.example.lostfound.dto.response.UserSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    AdminDashboardResponse getDashboardStats();
    PagedResponse<UserSummaryResponse> getAllUsers(Pageable pageable);
    UserSummaryResponse updateUserRole(Long userId, UpdateRoleRequest request, Long currentAdminId);
    void deleteUser(Long userId, Long currentAdminId);
    PagedResponse<ItemResponse> getAllItems(Pageable pageable);
    PagedResponse<ClaimResponse> getAllClaims(Pageable pageable);
    void deleteItem(Long itemId);
    void deleteClaim(Long claimId);
}
