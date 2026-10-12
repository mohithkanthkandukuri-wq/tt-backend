package com.example.lostfound.repository;

import com.example.lostfound.entity.Item;
import com.example.lostfound.entity.ItemCategory;
import com.example.lostfound.entity.ItemStatus;
import com.example.lostfound.entity.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

    Page<Item> findByReporterId(Long reporterId, Pageable pageable);

    List<Item> findTop6ByOrderByCreatedAtDesc();

    long countByReportType(ReportType reportType);

    long countByStatus(ItemStatus status);

    long countByReportTypeAndStatus(ReportType reportType, ItemStatus status);
}
