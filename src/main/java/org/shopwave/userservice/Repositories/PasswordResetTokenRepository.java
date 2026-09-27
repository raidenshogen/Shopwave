package org.shopwave.userservice.Repositories;

import org.shopwave.userservice.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
public interface PasswordResetTokenRepository 
    extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Transactional 
    @Query("UPDATE PasswordResetToken p SET p.isUsed = true WHERE p.user.id = :userId")
    void invalidateAllByUserId(UUID userId);
}