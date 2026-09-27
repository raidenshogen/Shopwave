package org.shopwave.userservice.Services;

public interface EmailService {
    void sendWelcomeEmail(String to, String firstName);
    void sendPasswordResetEmail(String to, String resetToken);
}