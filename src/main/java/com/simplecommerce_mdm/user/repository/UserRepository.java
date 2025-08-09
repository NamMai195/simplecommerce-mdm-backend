package com.simplecommerce_mdm.user.repository;

import com.simplecommerce_mdm.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Existing methods
    Optional<User> findByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE CONCAT(u.firstName, ' ', u.lastName) = :fullName")
    Optional<User> findByFullName(@Param("fullName") String fullName);

    // Additional methods for user management
    Optional<User> findByUuid(UUID uuid);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.isActive = :isActive")
    Page<User> findByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.emailVerifiedAt IS NOT NULL")
    Page<User> findByEmailVerified(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.emailVerifiedAt IS NULL")
    Page<User> findByEmailNotVerified(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.phoneVerifiedAt IS NOT NULL")
    Page<User> findByPhoneVerified(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.phoneVerifiedAt IS NULL")
    Page<User> findByPhoneNotVerified(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :fromDate")
    Page<User> findByLastLoginAfter(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "(:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "u.phoneNumber LIKE CONCAT('%', :searchTerm, '%')) AND " +
            "(:roleName IS NULL OR :roleName = '' OR EXISTS (SELECT r FROM u.roles r WHERE r.roleName = :roleName)) AND "
            +
            "(:isActive IS NULL OR u.isActive = :isActive) AND " +
            "(:emailVerified IS NULL OR " +
            "(:emailVerified = true AND u.emailVerifiedAt IS NOT NULL) OR " +
            "(:emailVerified = false AND u.emailVerifiedAt IS NULL)) AND " +
            "(:phoneVerified IS NULL OR " +
            "(:phoneVerified = true AND u.phoneVerifiedAt IS NOT NULL) OR " +
            "(:phoneVerified = false AND u.phoneVerifiedAt IS NULL))")
    Page<User> findUsersWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("roleName") String roleName,
            @Param("isActive") Boolean isActive,
            @Param("emailVerified") Boolean emailVerified,
            @Param("phoneVerified") Boolean phoneVerified,
            Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerifiedAt IS NOT NULL")
    Long countEmailVerifiedUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :fromDate")
    Long countNewUsersFromDate(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NOT NULL")
    Page<User> findDeletedUsers(Pageable pageable);
}