package com.shophub.service;

import com.shophub.dto.Dtos.DashboardStats;
import com.shophub.dto.Dtos.UserResponse;
import com.shophub.model.OrderStatus;
import com.shophub.model.Role;
import com.shophub.model.User;
import com.shophub.repository.ProductRepository;
import com.shophub.repository.PurchaseOrderRepository;
import com.shophub.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class AdminService {
    private final UserRepository users;
    private final ProductRepository products;
    private final PurchaseOrderRepository orders;

    public AdminService(UserRepository users, ProductRepository products, PurchaseOrderRepository orders) {
        this.users = users;
        this.products = products;
        this.orders = orders;
    }

    public static UserResponse toDto(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isApproved(), u.isEnabled());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return users.findAll().stream().map(AdminService::toDto).toList();
    }

    public UserResponse approveSeller(Long id) {
        User u = find(id);
        if (u.getRole() != Role.SELLER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only sellers need approval");
        }
        u.setApproved(true);
        return toDto(users.save(u));
    }

    public UserResponse toggleEnabled(Long id, User admin) {
        User u = find(id);
        if (u.getId().equals(admin.getId()) || u.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin accounts cannot be disabled");
        }
        u.setEnabled(!u.isEnabled());
        return toDto(users.save(u));
    }

    @Transactional(readOnly = true)
    public DashboardStats dashboard() {
        BigDecimal revenue = orders.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(o -> o.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardStats(users.count(), users.countByRole(Role.CUSTOMER), users.countByRole(Role.SELLER),
                users.countByRoleAndApproved(Role.SELLER, false), products.countByActiveTrue(), orders.count(), revenue);
    }

    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
