package com.hms.auth.repository;

import com.hms.auth.entity.RevokedRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedRefreshTokenRepository extends JpaRepository<RevokedRefreshToken, Long> {
    boolean existsByTokenHash(String tokenHash);
}
