package com.example.lostfound.service.impl;

import com.example.lostfound.dto.request.ItemCreateRequest;
import com.example.lostfound.dto.request.ItemUpdateRequest;
import com.example.lostfound.dto.response.ItemResponse;
import com.example.lostfound.dto.response.PagedResponse;
import com.example.lostfound.entity.Item;
import com.example.lostfound.entity.ItemCategory;
import com.example.lostfound.entity.ItemStatus;
import com.example.lostfound.entity.ReportType;
import com.example.lostfound.entity.User;
import com.example.lostfound.exception.ForbiddenException;
import com.example.lostfound.exception.ResourceNotFoundException;
import com.example.lostfound.repository.ClaimRepository;
import com.example.lostfound.repository.ItemRepository;
import com.example.lostfound.service.FileStorageService;
import com.example.lostfound.service.ItemService;
import com.example.lostfound.service.UserService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public ItemServiceImpl(ItemRepository itemRepository,
                           ClaimRepository claimRepository,
                           UserService userService,
                           FileStorageService fileStorageService) {
        this.itemRepository = itemRepository;
        this.claimRepository = claimRepository;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public ItemResponse createItem(ItemCreateRequest request, Long userId) {
        User user = userService.getUserById(userId);

        Item item = new Item(
                request.getTitle().trim(),
                request.getDescription().trim(),
                request.getCategory(),
                request.getReportType(),
                request.getLocation().trim(),
                request.getIncidentDate(),
                user
        );

        Item savedItem = itemRepository.save(item);
        return new ItemResponse(savedItem, 0);
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long itemId, ItemUpdateRequest request, Long userId, boolean isAdmin) {
        Item item = getItemEntity(itemId);

        if (!item.getReporter().getId().equals(userId) && !isAdmin) {
            throw new ForbiddenException("You do not have permission to modify this report");
        }

        item.setTitle(request.getTitle().trim());
        item.setDescription(request.getDescription().trim());
        item.setCategory(request.getCategory());
        item.setReportType(request.getReportType());
        item.setLocation(request.getLocation().trim());
        item.setIncidentDate(request.getIncidentDate());

        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }

        Item updated = itemRepository.save(item);
        int claimCount = claimRepository.findByItemId(itemId).size();
        return new ItemResponse(updated, claimCount);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId, Long userId, boolean isAdmin) {
        Item item = getItemEntity(itemId);

        if (!item.getReporter().getId().equals(userId) && !isAdmin) {
            throw new ForbiddenException("You do not have permission to delete this report");
        }

        if (item.getImagePath() != null) {
            fileStorageService.deleteFile(item.getImagePath());
        }

        itemRepository.delete(item);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long itemId) {
        Item item = getItemEntity(itemId);
        int claimCount = claimRepository.findByItemId(itemId).size();
        return new ItemResponse(item, claimCount);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ItemResponse> getMyItems(Long userId, Pageable pageable) {
        Page<Item> items = itemRepository.findByReporterId(userId, pageable);
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
    public PagedResponse<ItemResponse> searchItems(String query, ReportType reportType, ItemCategory category,
                                                  String location, ItemStatus status, Pageable pageable) {
        Specification<Item> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(query)) {
                String searchPattern = "%" + query.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), searchPattern),
                        cb.like(cb.lower(root.get("description")), searchPattern)
                ));
            }

            if (reportType != null) {
                predicates.add(cb.equal(root.get("reportType"), reportType));
            }

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (StringUtils.hasText(location)) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%"));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Item> items = itemRepository.findAll(spec, pageable);
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
    public List<ItemResponse> getRecentItems() {
        return itemRepository.findTop6ByOrderByCreatedAtDesc().stream()
                .map(item -> new ItemResponse(item, claimRepository.findByItemId(item.getId()).size()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemResponse uploadItemImage(Long itemId, MultipartFile file, Long userId, boolean isAdmin) {
        Item item = getItemEntity(itemId);

        if (!item.getReporter().getId().equals(userId) && !isAdmin) {
            throw new ForbiddenException("You do not have permission to upload image for this report");
        }

        // Delete old image if present
        if (item.getImagePath() != null) {
            fileStorageService.deleteFile(item.getImagePath());
        }

        String fileName = fileStorageService.storeFile(file);
        item.setImagePath(fileName);
        Item updated = itemRepository.save(item);

        int claimCount = claimRepository.findByItemId(itemId).size();
        return new ItemResponse(updated, claimCount);
    }

    @Override
    @Transactional
    public ItemResponse deleteItemImage(Long itemId, Long userId, boolean isAdmin) {
        Item item = getItemEntity(itemId);

        if (!item.getReporter().getId().equals(userId) && !isAdmin) {
            throw new ForbiddenException("You do not have permission to delete image for this report");
        }

        if (item.getImagePath() != null) {
            fileStorageService.deleteFile(item.getImagePath());
            item.setImagePath(null);
            itemRepository.save(item);
        }

        int claimCount = claimRepository.findByItemId(itemId).size();
        return new ItemResponse(item, claimCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Item getItemEntity(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));
    }
}
