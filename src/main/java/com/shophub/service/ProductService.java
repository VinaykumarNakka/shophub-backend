package com.shophub.service;

import com.shophub.dto.Dtos.ProductRequest;
import com.shophub.dto.Dtos.ProductResponse;
import com.shophub.model.Category;
import com.shophub.model.Product;
import com.shophub.model.Role;
import com.shophub.model.User;
import com.shophub.repository.CategoryRepository;
import com.shophub.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;

    public ProductService(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    public static ProductResponse toDto(Product p) {
        Category c = p.getCategory();
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock(),
                p.getImageUrl(), c == null ? null : c.getId(), c == null ? null : c.getName(),
                p.getSeller().getId(), p.getSeller().getName());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> search(String q, Long categoryId) {
        String needle = q == null ? "" : q.trim().toLowerCase();
        return products.findByActiveTrueOrderByIdDesc().stream()
                .filter(p -> needle.isEmpty() || p.getName().toLowerCase().contains(needle)
                        || (p.getDescription() != null && p.getDescription().toLowerCase().contains(needle)))
                .filter(p -> categoryId == null || (p.getCategory() != null && p.getCategory().getId().equals(categoryId)))
                .map(ProductService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return toDto(products.findById(id).filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found")));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> mine(User seller) {
        return products.findBySellerIdAndActiveTrueOrderByIdDesc(seller.getId()).stream().map(ProductService::toDto).toList();
    }

    public ProductResponse create(User seller, ProductRequest r) {
        requireApproved(seller);
        Product p = new Product();
        p.setSeller(seller);
        apply(p, r);
        return toDto(products.save(p));
    }

    public ProductResponse update(User seller, Long id, ProductRequest r) {
        requireApproved(seller);
        Product p = owned(seller, id);
        apply(p, r);
        return toDto(products.save(p));
    }

    /** Soft delete: order history keeps pointing at the product. */
    public void delete(User seller, Long id) {
        Product p = owned(seller, id);
        p.setActive(false);
        products.save(p);
    }

    private Product owned(User seller, Long id) {
        Product p = products.findById(id).filter(Product::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (!p.getSeller().getId().equals(seller.getId()) && seller.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This is not your product");
        }
        return p;
    }

    private void requireApproved(User seller) {
        if (!seller.isApproved()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your seller account is waiting for admin approval");
        }
    }

    private void apply(Product p, ProductRequest r) {
        p.setName(r.name().trim());
        p.setDescription(r.description());
        p.setPrice(r.price());
        p.setStock(r.stock());
        p.setImageUrl(r.imageUrl() == null || r.imageUrl().isBlank() ? null : r.imageUrl().trim());
        p.setCategory(r.categoryId() == null ? null : categories.findById(r.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown category")));
    }
}
