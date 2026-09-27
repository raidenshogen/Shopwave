package org.shopwave.userservice.Dto.response;

import lombok.Builder;
import lombok.Data;
import org.shopwave.userservice.entities.Role;

import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private Boolean isActive;
    private Boolean isVerified;
}
