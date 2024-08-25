package com.example.demoe.Dto.Address;

import com.example.demoe.Entity.Address.Address;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {
    private String message;
    private Address address;
    public AddressDto(String message,Address address) {
        this.message = message;
        this.address = address;
    }
    public AddressDto(String message) {
        this.message = message;
    }
}
