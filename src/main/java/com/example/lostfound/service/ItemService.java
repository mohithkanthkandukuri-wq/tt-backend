package com.example.lostfound.service;

import com.example.lostfound.dto.request.ItemCreateRequest;
import com.example.lostfound.dto.request.ItemUpdateRequest;
import com.example.lostfound.dto.response.ItemResponse;
import com.example.lostfound.dto.response.PagedResponse;
import com.example.lostfound.entity.Item;
import com.example.lostfound.entity.ItemCategory;
import com.example.lostfound.entity.ItemStatus;
import com.example.lostfound.entity.ReportType;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {
    ItemResponse createItem(ItemCreateRequest request, Long userId);
    ItemResponse updateItem(Long itemId, ItemUpdateRequest request, Long userId, boolean isAdmin);
    void deleteItem(Long itemId, Long userId, boolean isAdmin);
    ItemResponse getItemById(Long itemId);
    PagedResponse<ItemResponse> getMyItems(Long userId, Pageable pageable);
    PagedResponse<ItemResponse> searchItems(String query, ReportType reportType, ItemCategory category,
                                           String location, ItemStatus status, Pageable pageable);
    List<ItemResponse> getRecentItems();
    ItemResponse uploadItemImage(Long itemId, MultipartFile file, Long userId, boolean isAdmin);
    ItemResponse deleteItemImage(Long itemId, Long userId, boolean isAdmin);
    Item getItemEntity(Long itemId);
}
