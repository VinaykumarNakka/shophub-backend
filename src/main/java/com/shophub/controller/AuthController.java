package com.shophub.controller;

import com.shophub.dto.Dtos.*;
import com.shophub.model.User;
import com.shophub.service.AdminService;
import com.shophub.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) { return auth.register(req); }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) { return auth.login(req); }

    /** Current user (used by the frontend to refresh e.g. the seller "approved" flag). */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User user) { return AdminService.toDto(user); }
}
