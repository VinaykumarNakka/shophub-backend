package com.shophub.config;

import com.shophub.model.*;
import com.shophub.repository.CategoryRepository;
import com.shophub.repository.ProductRepository;
import com.shophub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Creates demo accounts, categories and products on first start (only when the DB is empty). */
@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository users, CategoryRepository categories, ProductRepository products, PasswordEncoder encoder) {
        this.users = users;
        this.categories = categories;
        this.products = products;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (users.count() > 0) return;

        user("Admin", "admin@shophub.com", "admin123", Role.ADMIN, true);
        User seller = user("Demo Seller", "seller@shophub.com", "seller123", Role.SELLER, true);
        user("Pending Seller", "pending@shophub.com", "seller123", Role.SELLER, false);
        user("Demo Customer", "customer@shophub.com", "customer123", Role.CUSTOMER, true);

        Category electronics = categories.save(new Category("Electronics"));
        Category fashion = categories.save(new Category("Fashion"));
        Category home = categories.save(new Category("Home & Kitchen"));
        Category books = categories.save(new Category("Books"));

        product(seller, electronics, "Wireless Headphones", "Bluetooth over-ear headphones with 30-hour battery life.", "2999.00", 40, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=300&fit=crop");
        product(seller, electronics, "Smart Watch", "Fitness tracking, heart-rate monitor and notifications.", "4499.00", 25, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=300&fit=crop");
        product(seller, fashion, "Classic Denim Jacket", "Regular-fit denim jacket in washed blue.", "2499.00", 30, "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400&h=300&fit=crop");
        product(seller, fashion, "Running Shoes", "Lightweight breathable shoes for daily runs.", "3199.00", 50, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=300&fit=crop");
        product(seller, home, "Stainless Steel Water Bottle", "1 litre insulated bottle, keeps drinks cold for 24 hours.", "899.00", 80, "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=400&h=300&fit=crop");
        product(seller, books, "Clean Code", "A handbook of agile software craftsmanship.", "899.00", 60, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&h=300&fit=crop");
    }

    private User user(String name, String email, String password, Role role, boolean approved) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setRole(role);
        u.setApproved(approved);
        return users.save(u);
    }

    private void product(User seller, Category cat, String name, String desc, String price, int stock, String imageUrl) {
        Product p = new Product();
        p.setSeller(seller);
        p.setCategory(cat);
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setImageUrl(imageUrl);
        products.save(p);
    }
}