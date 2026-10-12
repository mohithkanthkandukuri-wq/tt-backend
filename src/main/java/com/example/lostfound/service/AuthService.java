package com.example.lostfound.service;

import com.example.lostfound.dto.request.LoginRequest;
import com.example.lostfound.dto.request.RegisterRequest;
import com.example.lostfound.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
}
