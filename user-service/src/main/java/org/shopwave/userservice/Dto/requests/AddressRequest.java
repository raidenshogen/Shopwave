package org.shopwave.userservice.Dto.requests;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {
    private String label;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String phone;
    @NotBlank
    private String street;
    @NotBlank
    private String city;
    private String state;
    @NotBlank
    private String country;
    @NotBlank
    private String zipCode;
    private Boolean isDefault;
}