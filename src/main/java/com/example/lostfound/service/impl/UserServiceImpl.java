package com.example.lostfound.service.impl;

import com.example.lostfound.dto.request.UpdateProfileRequest;
import com.example.lostfound.dto.response.UserSummaryResponse;
import com.example.lostfound.entity.User;
import com.example.lostfound.exception.ResourceNotFoundException;
import com.example.lostfound.repository.UserRepository;
import com.example.lostfound.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getUserProfile(Long userId) {
        User user = getUserById(userId);
        return new UserSummaryResponse(user);
    }

    @Override
    @Transactional
    public UserSummaryResponse updateUserProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }

        userRepository.save(user);
        return new UserSummaryResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
