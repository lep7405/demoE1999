package com.example.demoe.Entity;

import com.example.demoe.Entity.product.Discount;
import com.example.demoe.Entity.product.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Admin extends Account{

    @OneToMany(mappedBy = "admin")
    private List<Product> productList;

    public void addProduct(Product product){
        if(productList==null){
            productList=new ArrayList<>();
        }
        productList.add(product);
        product.setAdmin(this);
    }

    @OneToMany(mappedBy = "admin")
    private List<Discount> discountList;

    public void addDiscount(Discount discount){
        if(discountList==null){
            discountList=new ArrayList<>();
        }
        discountList.add(discount);
        discount.setAdmin(this);
    }
}
