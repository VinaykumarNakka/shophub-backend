package com.shophub.repository;

import com.shophub.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    List<PurchaseOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    @Query("select distinct o from PurchaseOrder o join o.items i where i.product.seller.id = :sellerId order by o.createdAt desc")
    List<PurchaseOrder> findForSeller(@Param("sellerId") Long sellerId);
}
