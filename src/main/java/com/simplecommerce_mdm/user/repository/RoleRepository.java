package com.simplecommerce_mdm.user.repository;

import com.simplecommerce_mdm.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByRoleName(String roleName);
    Optional<Role> findByRoleName(String roleName);
}