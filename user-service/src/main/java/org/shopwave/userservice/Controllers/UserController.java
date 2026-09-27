package org.shopwave.userservice.Controllers;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.shopwave.userservice.Dto.requests.AddressRequest;
import org.shopwave.userservice.Dto.requests.UpdateProfileRequest;
import org.shopwave.userservice.Dto.response.AddressResponse;
import org.shopwave.userservice.Dto.response.UserResponse;
import org.shopwave.userservice.Services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get my profile
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    // Update my profile
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(
                userService.updateProfile(userId, request));
    }

    // Get my addresses
    @GetMapping("/me/addresses")
    public ResponseEntity<List<AddressResponse>> getAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity.ok(userService.getAddresses(userId));
    }

    // Add new address
    @PostMapping("/me/addresses")
    public ResponseEntity<AddressResponse> addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        UUID userId = extractUserId(userDetails);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.addAddress(userId, request));
    }

    // Delete address
    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID addressId) {
        UUID userId = extractUserId(userDetails);
        userService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    // Admin only - get all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Admin only - toggle user status
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleUserStatus(
            @PathVariable UUID userId) {
        userService.toggleUserStatus(userId);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────
    // Private Helper Methods
    // ─────────────────────────────────────

    private UUID extractUserId(UserDetails userDetails) {
        // userDetails.getUsername() returns email
        // we need to get user from DB to get ID
        return userService.getUserIdByEmail(
                userDetails.getUsername());
    }
}
