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

        product(seller, electronics, "Wireless Headphones", "Bluetooth over-ear headphones with 30-hour battery life.", "59.99", 40, "headphones");
        product(seller, electronics, "Smart Watch", "Fitness tracking, heart-rate monitor and notifications.", "89.00", 25, "watch");
        product(seller, electronics, "Portable Power Bank 20000mAh", "Fast-charging power bank with dual USB ports.", "29.50", 100, "powerbank");
        product(seller, fashion, "Classic Denim Jacket", "Regular-fit denim jacket in washed blue.", "49.90", 30, "jacket");
        product(seller, fashion, "Running Shoes", "Lightweight breathable shoes for daily runs.", "64.00", 50, "shoes");
        product(seller, home, "Stainless Steel Water Bottle", "1 litre insulated bottle, keeps drinks cold for 24 hours.", "18.75", 80, "bottle");
        product(seller, home, "Non-stick Cookware Set", "5-piece induction-friendly cookware set.", "74.99", 15, "cookware");
        product(seller, books, "Clean Code", "A handbook of agile software craftsmanship.", "32.00", 60, "book");
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

    private void product(User seller, Category cat, String name, String desc, String price, int stock, String seed) {
        Product p = new Product();
        p.setSeller(seller);
        p.setCategory(cat);
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(new BigDecimal(price));
        p.setStock(stock);
        p.setImageUrl("https://picsum.photos/seed/shophub-" + seed + "/400/300");
        products.save(p);
    }
}
