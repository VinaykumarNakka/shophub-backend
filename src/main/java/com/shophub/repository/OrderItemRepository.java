package com.shophub.repository;

import com.shophub.model.OrderItem;
import com.shophub.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query("select i from OrderItem i where i.product.seller.id = :sellerId and i.purchaseOrder.status <> :excluded")
    List<OrderItem> findSoldBySeller(@Param("sellerId") Long sellerId, @Param("excluded") OrderStatus excluded);
}
