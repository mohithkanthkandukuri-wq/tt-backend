package com.example.lostfound.service;

import com.example.lostfound.dto.request.UpdateProfileRequest;
import com.example.lostfound.dto.response.UserSummaryResponse;
import com.example.lostfound.entity.User;

public interface UserService {
    UserSummaryResponse getUserProfile(Long userId);
    UserSummaryResponse updateUserProfile(Long userId, UpdateProfileRequest request);
    User getUserById(Long userId);
}
