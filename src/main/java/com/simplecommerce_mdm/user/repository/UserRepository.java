package com.simplecommerce_mdm.user.repository;

import com.simplecommerce_mdm.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA sẽ tự động tạo câu lệnh SELECT...WHERE email = ?
    Optional<User> findByEmail(String email);
}