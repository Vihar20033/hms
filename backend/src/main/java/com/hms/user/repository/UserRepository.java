package com.hms.user.repository;

import com.hms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * Restores a soft-deleted user account by setting {@code deleted = false}.
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET deleted = false WHERE id = :id", nativeQuery = true)
    int restore(@Param("id") Long id);

    /**
     * Finds a user regardless of soft-delete status (needed for login / downstream checks).
     */
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailIncludingDeleted(@Param("email") String email);
}
