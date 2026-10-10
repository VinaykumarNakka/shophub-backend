package com.shophub.controller;

import com.shophub.dto.Dtos.OrderResponse;
import com.shophub.dto.Dtos.PlaceOrderRequest;
import com.shophub.model.User;
import com.shophub.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Customer endpoints (ROLE_CUSTOMER). */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orders;

    public OrderController(OrderService orders) { this.orders = orders; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@AuthenticationPrincipal User user, @Valid @RequestBody PlaceOrderRequest req) {
        return orders.place(user, req);
    }

    @GetMapping
    public List<OrderResponse> mine(@AuthenticationPrincipal User user) { return orders.forCustomer(user); }

    @PutMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return orders.cancel(user, id);
    }
}
