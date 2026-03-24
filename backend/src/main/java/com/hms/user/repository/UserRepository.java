package com.hms.user.repository;

import com.hms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // SELECT * FROM users WHERE username = :username AND deleted = false
    Optional<User> findByUsernameAndDeletedFalse(String username);

    // SELECT * FROM users WHERE email = :email AND deleted = false
    Optional<User> findByEmailAndDeletedFalse(String email);

    // SELECT * FROM users WHERE username = :username
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameIncludingDeleted(
            @org.springframework.data.repository.query.Param("username") String username
    );

    // SELECT * FROM users WHERE email = :email
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailIncludingDeleted(
            @org.springframework.data.repository.query.Param("email") String email
    );

    // SELECT EXISTS(SELECT 1 FROM users WHERE username = :username AND deleted = false)
    boolean existsByUsernameAndDeletedFalse(String username);

    // SELECT EXISTS(SELECT 1 FROM users WHERE email = :email AND deleted = false)
    boolean existsByEmailAndDeletedFalse(String email);

    // Handled by findByUsernameAndDeletedFalse
    default Optional<User> findByUsername(String username) {
        return findByUsernameAndDeletedFalse(username);
    }

    // Handled by findByEmailAndDeletedFalse
    default Optional<User> findByEmail(String email) {
        return findByEmailAndDeletedFalse(email);
    }

    // Handled by existsByUsernameAndDeletedFalse
    default boolean existsByUsername(String username) {
        return existsByUsernameAndDeletedFalse(username);
    }

    // Handled by existsByEmailAndDeletedFalse
    default boolean existsByEmail(String email) {
        return existsByEmailAndDeletedFalse(email);
    }
}
