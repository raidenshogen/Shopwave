package org.shopwave.userservice.Services;

import org.shopwave.userservice.Dto.requests.AddressRequest;
import org.shopwave.userservice.Dto.requests.UpdateProfileRequest;
import org.shopwave.userservice.Dto.response.AddressResponse;
import org.shopwave.userservice.Dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse getProfile(UUID userId);
    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);
    AddressResponse addAddress(UUID userId, AddressRequest request);
    List<AddressResponse> getAddresses(UUID userId);
    void deleteAddress(UUID userId, UUID addressId);
    List<UserResponse> getAllUsers();
    void toggleUserStatus(UUID userId);
    UUID getUserIdByEmail(String email);
}
