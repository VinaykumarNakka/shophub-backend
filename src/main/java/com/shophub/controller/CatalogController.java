package com.shophub.controller;

import com.shophub.dto.Dtos.CategoryResponse;
import com.shophub.dto.Dtos.ProductResponse;
import com.shophub.repository.CategoryRepository;
import com.shophub.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Public, read-only catalogue. */
@RestController
@RequestMapping("/api")
public class CatalogController {
    private final ProductService products;
    private final CategoryRepository categories;

    public CatalogController(ProductService products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    @GetMapping("/products")
    public List<ProductResponse> list(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) Long categoryId) {
        return products.search(q, categoryId);
    }

    @GetMapping("/products/{id}")
    public ProductResponse one(@PathVariable Long id) { return products.get(id); }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return categories.findAll().stream().map(c -> new CategoryResponse(c.getId(), c.getName())).toList();
    }
}
