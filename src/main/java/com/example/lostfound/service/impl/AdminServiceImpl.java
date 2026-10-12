package com.example.lostfound.service.impl;

import com.example.lostfound.dto.request.UpdateRoleRequest;
import com.example.lostfound.dto.response.AdminDashboardResponse;
import com.example.lostfound.dto.response.ClaimResponse;
import com.example.lostfound.dto.response.ItemResponse;
import com.example.lostfound.dto.response.PagedResponse;
import com.example.lostfound.dto.response.UserSummaryResponse;
import com.example.lostfound.entity.Claim;
import com.example.lostfound.entity.ClaimStatus;
import com.example.lostfound.entity.Item;
import com.example.lostfound.entity.ItemStatus;
import com.example.lostfound.entity.ReportType;
import com.example.lostfound.entity.Role;
import com.example.lostfound.entity.User;
import com.example.lostfound.exception.BadRequestException;
import com.example.lostfound.exception.ResourceNotFoundException;
import com.example.lostfound.repository.ClaimRepository;
import com.example.lostfound.repository.ItemRepository;
import com.example.lostfound.repository.UserRepository;
import com.example.lostfound.service.AdminService;
import com.example.lostfound.service.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;
    private final FileStorageService fileStorageService;

    public AdminServiceImpl(UserRepository userRepository,
                            ItemRepository itemRepository,
                            ClaimRepository claimRepository,
                            FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.claimRepository = claimRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        AdminDashboardResponse response = new AdminDashboardResponse();

        response.setTotalUsers(userRepository.count());
        response.setTotalLostReports(itemRepository.countByReportType(ReportType.LOST));
        response.setTotalFoundReports(itemRepository.countByReportType(ReportType.FOUND));
        response.setOpenReports(itemRepository.countByStatus(ItemStatus.OPEN) + itemRepository.countByStatus(ItemStatus.CLAIM_PENDING));
        response.setReturnedItems(itemRepository.countByStatus(ItemStatus.RETURNED));
        response.setPendingClaims(claimRepository.countByStatus(ClaimStatus.PENDING));

        // Recent items
        List<ItemResponse> recentItems = itemRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent().stream()
                .map(item -> new ItemResponse(item, claimRepository.findByItemId(item.getId()).size()))
                .collect(Collectors.toList());
        response.setRecentReports(recentItems);

        // Recent claims
        List<ClaimResponse> recentClaims = claimRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent().stream()
                .map(ClaimResponse::new)
                .collect(Collectors.toList());
        response.setRecentClaims(recentClaims);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        List<UserSummaryResponse> content = users.getContent().stream()
                .map(UserSummaryResponse::new)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages(),
                users.isLast()
        );
    }

    @Override
    @Transactional
    public UserSummaryResponse updateUserRole(Long userId, UpdateRoleRequest request, Long currentAdminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (userId.equals(currentAdminId) && request.getRole() != Role.ADMIN) {
            throw new BadRequestException("Administrators cannot demote their own account");
        }

        if (user.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot demote the only administrator in the system");
            }
        }

        user.setRole(request.getRole());
        userRepository.save(user);

        return new UserSummaryResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, Long currentAdminId) {
        if (userId.equals(currentAdminId)) {
            throw new BadRequestException("You cannot delete your own administrator account");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Cannot delete the only administrator in the system");
            }
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ItemResponse> getAllItems(Pageable pageable) {
        Page<Item> items = itemRepository.findAll(pageable);
        List<ItemResponse> content = items.getContent().stream()
                .map(item -> new ItemResponse(item, claimRepository.findByItemId(item.getId()).size()))
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                items.getNumber(),
                items.getSize(),
                items.getTotalElements(),
                items.getTotalPages(),
                items.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ClaimResponse> getAllClaims(Pageable pageable) {
        Page<Claim> claims = claimRepository.findAll(pageable);
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
    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (item.getImagePath() != null) {
            fileStorageService.deleteFile(item.getImagePath());
        }

        itemRepository.delete(item);
    }

    @Override
    @Transactional
    public void deleteClaim(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        Item item = claim.getItem();
        claimRepository.delete(claim);

        // If item was pending claims and now has none, revert to OPEN
        if (item.getStatus() == ItemStatus.CLAIM_PENDING) {
            List<Claim> remaining = claimRepository.findByItemId(item.getId());
            boolean hasPending = remaining.stream().anyMatch(c -> c.getStatus() == ClaimStatus.PENDING);
            if (!hasPending) {
                item.setStatus(ItemStatus.OPEN);
                itemRepository.save(item);
            }
        }
    }
}
