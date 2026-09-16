package org.shopwave.userservice.Services;

import org.shopwave.userservice.entities.User;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    boolean validateToken(String token);
    String extractEmail(String token);
    boolean isTokenExpired(String token);
}
