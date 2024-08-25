package com.example.demoe.Dto.Address;

import com.example.demoe.Entity.Address.Address;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListAddressDto {
    private String message;
    private List<Address> addressList;
    public ListAddressDto(String message, List<Address> addressList) {
        this.message = message;
        this.addressList = addressList;
    }
    public ListAddressDto(String message) {
        this.message = message;
    }
}
