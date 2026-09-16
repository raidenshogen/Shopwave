package org.shopwave.userservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.UUID;
@Builder
@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String label;
   private String firstName;
   private String lastName;
    private String street;
    private String city;
    private String phone;
    private String country;
    private String zipCode;
    private Boolean isDefault;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
