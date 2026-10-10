package com.shophub.controller;

import com.shophub.dto.Dtos.*;
import com.shophub.model.User;
import com.shophub.service.OrderService;
import com.shophub.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Seller endpoints (ROLE_SELLER). */
@RestController
@RequestMapping("/api/seller")
public class SellerController {
    private final ProductService products;
    private final OrderService orders;

    public SellerController(ProductService products, OrderService orders) {
        this.products = products;
        this.orders = orders;
    }

    @GetMapping("/products")
    public List<ProductResponse> myProducts(@AuthenticationPrincipal User seller) { return products.mine(seller); }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@AuthenticationPrincipal User seller, @Valid @RequestBody ProductRequest req) {
        return products.create(seller, req);
    }

    @PutMapping("/products/{id}")
    public ProductResponse update(@AuthenticationPrincipal User seller, @PathVariable Long id,
                                  @Valid @RequestBody ProductRequest req) {
        return products.update(seller, id, req);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User seller, @PathVariable Long id) {
        products.delete(seller, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders")
    public List<OrderResponse> myOrders(@AuthenticationPrincipal User seller) { return orders.forSeller(seller); }

    @PutMapping("/orders/{id}/status")
    public OrderResponse updateStatus(@AuthenticationPrincipal User seller, @PathVariable Long id,
                                      @Valid @RequestBody StatusRequest req) {
        return orders.updateStatus(seller, id, req.status());
    }

    @GetMapping("/reports")
    public SalesReport report(@AuthenticationPrincipal User seller) { return orders.report(seller); }
}
