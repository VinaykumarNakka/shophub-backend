package com.shophub.service;

import com.shophub.dto.Dtos.*;
import com.shophub.model.*;
import com.shophub.repository.OrderItemRepository;
import com.shophub.repository.ProductRepository;
import com.shophub.repository.PurchaseOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class OrderService {
    private final PurchaseOrderRepository orders;
    private final ProductRepository products;
    private final OrderItemRepository orderItems;

    public OrderService(PurchaseOrderRepository orders, ProductRepository products, OrderItemRepository orderItems) {
        this.orders = orders;
        this.products = products;
        this.orderItems = orderItems;
    }

    // ---------- mapping ----------
    /** sellerId != null -> only that seller's lines (and their subtotal) are shown. */
    static OrderResponse toDto(PurchaseOrder o, Long sellerId) {
        List<OrderItemResponse> lines = o.getItems().stream()
                .filter(i -> sellerId == null || i.getProduct().getSeller().getId().equals(sellerId))
                .map(i -> new OrderItemResponse(i.getProduct().getId(), i.getProduct().getName(),
                        i.getProduct().getImageUrl(), i.getQuantity(), i.getPrice()))
                .toList();
        BigDecimal total = sellerId == null ? o.getTotal() : lines.stream()
                .map(l -> l.price().multiply(BigDecimal.valueOf(l.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderResponse(o.getId(), o.getCustomer().getName(), total, o.getStatus(),
                o.getCreatedAt(), o.getShippingAddress(), lines);
    }

    // ---------- customer ----------
    public OrderResponse place(User customer, PlaceOrderRequest req) {
        PurchaseOrder order = new PurchaseOrder();
        order.setCustomer(customer);
        order.setShippingAddress(req.shippingAddress().trim());
        BigDecimal total = BigDecimal.ZERO;
        for (CartLine line : req.items()) {
            Product p = products.findById(line.productId()).filter(Product::isActive)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "A product in your cart is no longer available"));
            if (p.getStock() < line.quantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Only " + p.getStock() + " left of \"" + p.getName() + "\"");
            }
            p.setStock(p.getStock() - line.quantity());
            OrderItem item = new OrderItem();
            item.setProduct(p);
            item.setQuantity(line.quantity());
            item.setPrice(p.getPrice());
            order.addItem(item);
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(line.quantity())));
        }
        order.setTotal(total);
        return toDto(orders.save(order), null);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> forCustomer(User customer) {
        return orders.findByCustomerIdOrderByCreatedAtDesc(customer.getId()).stream().map(o -> toDto(o, null)).toList();
    }

    public OrderResponse cancel(User customer, Long id) {
        PurchaseOrder o = find(id);
        if (!o.getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This is not your order");
        }
        if (o.getStatus() != OrderStatus.PLACED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only orders that are still 'PLACED' can be cancelled");
        }
        restock(o);
        o.setStatus(OrderStatus.CANCELLED);
        return toDto(o, null);
    }

    // ---------- seller ----------
    @Transactional(readOnly = true)
    public List<OrderResponse> forSeller(User seller) {
        return orders.findForSeller(seller.getId()).stream().map(o -> toDto(o, seller.getId())).toList();
    }

    public OrderResponse updateStatus(User seller, Long id, OrderStatus next) {
        PurchaseOrder o = find(id);
        boolean hasItemOfSeller = o.getItems().stream().anyMatch(i -> i.getProduct().getSeller().getId().equals(seller.getId()));
        if (!hasItemOfSeller) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order has none of your products");
        }
        if (next == OrderStatus.PLACED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose CONFIRMED, SHIPPED, DELIVERED or CANCELLED");
        }
        OrderStatus current = o.getStatus();
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is already " + current);
        }
        if (next != OrderStatus.CANCELLED && next.ordinal() <= current.ordinal()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status can only move forward");
        }
        if (next == OrderStatus.CANCELLED) restock(o);
        o.setStatus(next);
        return toDto(o, seller.getId());
    }

    @Transactional(readOnly = true)
    public SalesReport report(User seller) {
        List<OrderItem> sold = orderItems.findSoldBySeller(seller.getId(), OrderStatus.CANCELLED);
        BigDecimal revenue = BigDecimal.ZERO;
        long units = 0;
        Map<String, BigDecimal> revenueByProduct = new HashMap<>();
        Map<String, Long> unitsByProduct = new HashMap<>();
        Set<Long> orderIds = new HashSet<>();
        for (OrderItem i : sold) {
            BigDecimal line = i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
            revenue = revenue.add(line);
            units += i.getQuantity();
            String name = i.getProduct().getName();
            revenueByProduct.merge(name, line, BigDecimal::add);
            unitsByProduct.merge(name, (long) i.getQuantity(), Long::sum);
            orderIds.add(i.getPurchaseOrder().getId());
        }
        List<ProductSales> top = revenueByProduct.entrySet().stream()
                .map(e -> new ProductSales(e.getKey(), unitsByProduct.get(e.getKey()), e.getValue()))
                .sorted(Comparator.comparing(ProductSales::revenue, Comparator.reverseOrder()))
                .limit(5)
                .toList();
        return new SalesReport(revenue, orderIds.size(), units, top);
    }

    // ---------- admin ----------
    @Transactional(readOnly = true)
    public List<OrderResponse> all() {
        return orders.findAllByOrderByCreatedAtDesc().stream().map(o -> toDto(o, null)).toList();
    }

    // ---------- helpers ----------
    private PurchaseOrder find(Long id) {
        return orders.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private void restock(PurchaseOrder o) {
        for (OrderItem i : o.getItems()) {
            Product p = i.getProduct();
            p.setStock(p.getStock() + i.getQuantity());
        }
    }
}
