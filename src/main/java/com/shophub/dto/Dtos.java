package com.shophub.dto;

import com.shophub.model.OrderStatus;
import com.shophub.model.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** All request/response payloads in one place (Java records). */
public final class Dtos {
    private Dtos() {}

    // ---- auth ----
    public record RegisterRequest(@NotBlank String name, @NotBlank @Email String email,
                                  @NotBlank @Size(min = 6, message = "must be at least 6 characters") String password,
                                  Role role) {}
    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record AuthResponse(String token, Long id, String name, String email, Role role, boolean approved) {}
    public record UserResponse(Long id, String name, String email, Role role, boolean approved, boolean enabled) {}

    // ---- catalogue ----
    public record CategoryResponse(Long id, String name) {}
    public record ProductRequest(@NotBlank String name, String description,
                                 @NotNull @DecimalMin(value = "0.01", message = "must be greater than 0") BigDecimal price,
                                 @Min(0) int stock, String imageUrl, Long categoryId) {}
    public record ProductResponse(Long id, String name, String description, BigDecimal price, int stock,
                                  String imageUrl, Long categoryId, String categoryName,
                                  Long sellerId, String sellerName) {}

    // ---- orders ----
    public record CartLine(@NotNull Long productId, @Min(1) int quantity) {}
    public record PlaceOrderRequest(@NotEmpty List<@Valid CartLine> items, @NotBlank String shippingAddress) {}
    public record StatusRequest(@NotNull OrderStatus status) {}
    public record OrderItemResponse(Long productId, String productName, String imageUrl, int quantity, BigDecimal price) {}
    public record OrderResponse(Long id, String customerName, BigDecimal total, OrderStatus status,
                                LocalDateTime createdAt, String shippingAddress, List<OrderItemResponse> items) {}

    // ---- reports ----
    public record ProductSales(String productName, long units, BigDecimal revenue) {}
    public record SalesReport(BigDecimal totalRevenue, long totalOrders, long unitsSold, List<ProductSales> topProducts) {}
    public record DashboardStats(long totalUsers, long customers, long sellers, long pendingSellers,
                                 long activeProducts, long totalOrders, BigDecimal revenue) {}
}
