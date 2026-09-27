package org.shopwave.userservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "refresh_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String token;
    private LocalDateTime expiresAt;
    private Boolean isRevoked;
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    public boolean isValid() {
        return !isRevoked &&
                expiresAt.isAfter(LocalDateTime.now());
    }


}
