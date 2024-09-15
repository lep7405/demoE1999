package com.example.demoe.Controller.Order;

import com.example.demoe.Dto.Cart.CartItemDto;
import com.example.demoe.Entity.Address.Address;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class createOrder1Request {

    private List<CartItemDto> cartItemDtoList;
    private Address address;
}
