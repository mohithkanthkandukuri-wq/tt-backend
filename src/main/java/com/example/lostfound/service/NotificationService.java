package com.example.lostfound.service;

import com.example.lostfound.dto.response.NotificationResponse;
import com.example.lostfound.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    void createNotification(User user, String title, String message);
    List<NotificationResponse> getRecentNotifications(Long userId);
    Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
    long getUnreadCount(Long userId);
}
