package org.shopwave.userservice.Services.Imp;


import lombok.RequiredArgsConstructor;
import org.shopwave.userservice.Dto.requests.LoginRequest;
import org.shopwave.userservice.Dto.requests.RegisterRequest;
import org.shopwave.userservice.Dto.requests.ResetPasswordRequest;
import org.shopwave.userservice.Dto.response.AuthResponse;
import org.shopwave.userservice.Dto.response.UserResponse;
import org.shopwave.userservice.Repositories.PasswordResetTokenRepository;
import org.shopwave.userservice.Repositories.RefreshTokenRepository;
import org.shopwave.userservice.Repositories.UserRepository;
import org.shopwave.userservice.entities.PasswordResetToken;
import org.shopwave.userservice.entities.RefreshToken;
import org.shopwave.userservice.entities.Role;
import org.shopwave.userservice.entities.User;
import org.shopwave.userservice.Services.AuthService;
import org.shopwave.userservice.Services.EmailService;
import org.shopwave.userservice.Services.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import org.shopwave.userservice.entities.AuthProvider;
@Service
@Builder
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;
    
    
   @Override
@Transactional
public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
        throw new RuntimeException("Email already exists");
    }

    User user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.CUSTOMER)
            .authProvider(AuthProvider.LOCAL)
            .isActive(true)
            .isVerified(true)
            .build();

    userRepository.save(user);

    // Send welcome email
    emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    saveRefreshToken(user, refreshToken);

    return buildAuthResponse(accessToken, refreshToken, user);
}

    @Override
    public AuthResponse login(LoginRequest request) {

        // 1. Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Wrong password");
        }

        // 3. Check account valid
        if (!user.isAccountValid()) {
            throw new RuntimeException("Account is not active or not verified");
        }

        // 4. Revoke old tokens
        refreshTokenRepository.revokeAllByUserId(user.getId());

        // 5. Generate new tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 6. Save new refresh token
        saveRefreshToken(user, refreshToken);

        // 7. Return response
        return buildAuthResponse(accessToken, refreshToken, user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {

        // 1. Find token in DB
        RefreshToken token = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // 2. Check if valid
        if (!token.isValid()) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        // 3. Generate new access token
        String newAccessToken = jwtService.generateAccessToken(token.getUser());

        // 4. Return response
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900000L)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setIsRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

   @Override
@Transactional
public void forgotPassword(String email) {

    // 1. Find user
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // 2. Invalidate old tokens
    resetTokenRepository.invalidateAllByUserId(user.getId());

    // 3. Generate new token
    String resetToken = UUID.randomUUID().toString();

    // 4. Save token
    PasswordResetToken passwordResetToken = PasswordResetToken.builder()
            .user(user)
            .token(resetToken)
            .isUsed(false)
            .expiresAt(LocalDateTime.now().plusMinutes(15))
            .build();

    resetTokenRepository.save(passwordResetToken);

    // 5. Send email
    emailService.sendPasswordResetEmail(email, resetToken);
}

@Override
@Transactional
public void resetPassword(ResetPasswordRequest request) {

    // 1. Find token
    PasswordResetToken resetToken = resetTokenRepository
            .findByToken(request.getToken())
            .orElseThrow(() -> new RuntimeException("Invalid token"));

    // 2. Check if valid
    if (!resetToken.isValid()) {
        throw new RuntimeException("Token expired or already used");
    }

    // 3. Update password
    User user = resetToken.getUser();
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);

    // 4. Mark token as used
    resetToken.setIsUsed(true);
    resetTokenRepository.save(resetToken);
}


    // ─────────────────────────────────────
    // Private Helper Methods
    // ─────────────────────────────────────

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .isRevoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(String accessToken,
                                           String refreshToken,
                                           User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900000L)
                .user(UserResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .isActive(user.getIsActive())
                        .isVerified(user.getIsVerified())
                        .build())
                .build();
    }
}