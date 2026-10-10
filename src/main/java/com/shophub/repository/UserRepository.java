package com.shophub.repository;

import com.shophub.model.Role;
import com.shophub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(Role role);
    long countByRoleAndApproved(Role role, boolean approved);
}
