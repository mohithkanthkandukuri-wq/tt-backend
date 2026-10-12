package com.example.lostfound.controller;

import com.example.lostfound.dto.request.ItemCreateRequest;
import com.example.lostfound.dto.request.ItemUpdateRequest;
import com.example.lostfound.dto.response.ApiResponse;
import com.example.lostfound.dto.response.ItemResponse;
import com.example.lostfound.dto.response.PagedResponse;
import com.example.lostfound.entity.ItemCategory;
import com.example.lostfound.entity.ItemStatus;
import com.example.lostfound.entity.ReportType;
import com.example.lostfound.entity.Role;
import com.example.lostfound.security.UserPrincipal;
import com.example.lostfound.service.FileStorageService;
import com.example.lostfound.service.ItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final FileStorageService fileStorageService;

    public ItemController(ItemService itemService, FileStorageService fileStorageService) {
        this.itemService = itemService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ItemResponse>> searchItems(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        ReportType reportTypeEnum = null;
        if (org.springframework.util.StringUtils.hasText(reportType)) {
            try {
                reportTypeEnum = ReportType.valueOf(reportType.trim().toUpperCase());
            } catch (Exception ignored) {}
        }

        ItemCategory categoryEnum = null;
        if (org.springframework.util.StringUtils.hasText(category)) {
            categoryEnum = ItemCategory.fromString(category);
        }

        ItemStatus statusEnum = null;
        if (org.springframework.util.StringUtils.hasText(status)) {
            try {
                statusEnum = ItemStatus.valueOf(status.trim().toUpperCase());
            } catch (Exception ignored) {}
        }

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        PagedResponse<ItemResponse> response = itemService.searchItems(query, reportTypeEnum, categoryEnum, location, statusEnum, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ItemResponse>> searchItemsAlias(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return searchItems(query, reportType, category, location, status, page, size, sort);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ItemResponse>> getRecentItems() {
        return ResponseEntity.ok(itemService.getRecentItems());
    }

    @GetMapping("/my")
    public ResponseEntity<PagedResponse<ItemResponse>> getMyItems(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        PagedResponse<ItemResponse> response = itemService.getMyItems(currentUser.getId(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        ItemResponse response = itemService.getItemById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ItemCreateRequest request) {
        ItemResponse response = itemService.createItem(request, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ItemUpdateRequest request) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        ItemResponse response = itemService.updateItem(id, request, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        itemService.deleteItem(id, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(new ApiResponse(true, "Report deleted successfully"));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ItemResponse> uploadItemImage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        ItemResponse response = itemService.uploadItemImage(id, file, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/images")
    public ResponseEntity<ItemResponse> deleteItemImage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        ItemResponse response = itemService.deleteItemImage(id, currentUser.getId(), isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ignored) {}

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
