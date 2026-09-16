package org.shopwave.userservice.Services.Imp;
import lombok.RequiredArgsConstructor;
import org.shopwave.userservice.Services.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmailServiceImp implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Override
    public void sendWelcomeEmail(String to, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to ShopWave!");
        message.setText(
            "Hi " + firstName + ",\n\n" +
            "Welcome to ShopWave! We are glad to have you.\n\n" +
            "Best regards,\nShopWave Team"
        );
        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reset Your Password");
        message.setText(
            "Hi,\n\n" +
            "Click the link below to reset your password:\n" +
            resetLink + "\n\n" +
            "This link expires in 15 minutes.\n\n" +
            "If you did not request this, ignore this email.\n\n" +
            "Best regards,\nShopWave Team"
        );
        mailSender.send(message);
    }
}
