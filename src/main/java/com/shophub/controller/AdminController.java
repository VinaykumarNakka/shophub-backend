package com.shophub.controller;

import com.shophub.dto.Dtos.*;
import com.shophub.model.User;
import com.shophub.service.AdminService;
import com.shophub.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin endpoints (ROLE_ADMIN). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService admin;
    private final OrderService orders;

    public AdminController(AdminService admin, OrderService orders) {
        this.admin = admin;
        this.orders = orders;
    }

    @GetMapping("/dashboard")
    public DashboardStats dashboard() { return admin.dashboard(); }

    @GetMapping("/users")
    public List<UserResponse> users() { return admin.listUsers(); }

    @PutMapping("/users/{id}/approve")
    public UserResponse approve(@PathVariable Long id) { return admin.approveSeller(id); }

    @PutMapping("/users/{id}/toggle")
    public UserResponse toggle(@PathVariable Long id, @AuthenticationPrincipal User me) {
        return admin.toggleEnabled(id, me);
    }

    @GetMapping("/orders")
    public List<OrderResponse> allOrders() { return orders.all(); }
}
