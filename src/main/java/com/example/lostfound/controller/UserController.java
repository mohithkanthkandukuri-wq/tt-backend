package com.example.lostfound.controller;

import com.example.lostfound.dto.request.UpdateProfileRequest;
import com.example.lostfound.dto.response.UserSummaryResponse;
import com.example.lostfound.security.UserPrincipal;
import com.example.lostfound.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserSummaryResponse response = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserSummaryResponse> updateCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserSummaryResponse response = userService.updateUserProfile(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }
}
