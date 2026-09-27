package org.shopwave.userservice.Services.Imp;


import lombok.RequiredArgsConstructor;
import org.shopwave.userservice.Dto.requests.AddressRequest;
import org.shopwave.userservice.Dto.requests.UpdateProfileRequest;
import org.shopwave.userservice.Dto.response.AddressResponse;
import org.shopwave.userservice.Dto.response.UserResponse;
import org.shopwave.userservice.Repositories.AddressRepository;
import org.shopwave.userservice.Repositories.UserRepository;
import org.shopwave.userservice.entities.Address;
import org.shopwave.userservice.entities.User;
import org.shopwave.userservice.Services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Override
    public UserResponse getProfile(UUID userId) {
        User user = findUserById(userId);
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, 
                                      UpdateProfileRequest request) {
        User user = findUserById(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        userRepository.save(user);
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public AddressResponse addAddress(UUID userId, 
                                      AddressRequest request) {
        User user = findUserById(userId);

        // If new address is default
        // remove default from others
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.findByUserId(userId)
                    .forEach(addr -> {
                        addr.setIsDefault(false);
                        addressRepository.save(addr);
                    });
        }

        Address address = Address.builder()
                .user(user)
                .label(request.getLabel())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .street(request.getStreet())
                .city(request.getCity())
                .country(request.getCountry())
                .zipCode(request.getZipCode())
                .isDefault(request.getIsDefault())
                .build();

        addressRepository.save(address);
        return mapToAddressResponse(address);
    }

    @Override
    public List<AddressResponse> getAddresses(UUID userId) {
        return addressRepository.findByUserId(userId)
                .stream()
                .map(this::mapToAddressResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> 
                    new RuntimeException("Address not found"));

        // Make sure address belongs to this user
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        addressRepository.delete(address);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Override
    @Transactional
    public void toggleUserStatus(UUID userId) {
        User user = findUserById(userId);
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    // ─────────────────────────────────────
    // Private Helper Methods
    // ─────────────────────────────────────

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> 
                    new RuntimeException("User not found"));
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .build();
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .firstName(address.getFirstName())
                .lastName(address.getLastName())
                .phone(address.getPhone())
                .street(address.getStreet())
                .city(address.getCity())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .isDefault(address.getIsDefault())
                .build();
    }
    @Override
    public UUID getUserIdByEmail(String email) {
    return userRepository.findByEmail(email)
            .orElseThrow(() -> 
                new RuntimeException("User not found"))
            .getId();
    }
}
