package org.shopwave.userservice.Services;
import org.shopwave.userservice.Dto.requests.LoginRequest;
import org.shopwave.userservice.Dto.requests.RegisterRequest;
import org.shopwave.userservice.Dto.requests.ResetPasswordRequest;
import org.shopwave.userservice.Dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequest request);
}
