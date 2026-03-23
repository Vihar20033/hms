package com.hms.user.repository;

import com.hms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsernameAndDeletedFalse(String username);
    Optional<User> findByEmailAndDeletedFalse(String email);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameIncludingDeleted(
            @org.springframework.data.repository.query.Param("username") String username
    );

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailIncludingDeleted(
            @org.springframework.data.repository.query.Param("email") String email
    );

    boolean existsByUsernameAndDeletedFalse(String username);
    boolean existsByEmailAndDeletedFalse(String email);

    default Optional<User> findByUsername(String username) {
        return findByUsernameAndDeletedFalse(username);
    }

    default Optional<User> findByEmail(String email) {
        return findByEmailAndDeletedFalse(email);
    }

    default boolean existsByUsername(String username) {
        return existsByUsernameAndDeletedFalse(username);
    }

    default boolean existsByEmail(String email) {
        return existsByEmailAndDeletedFalse(email);
    }
}
