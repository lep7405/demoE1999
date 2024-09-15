package com.example.demoe.Entity.Shipping;

import com.example.demoe.Entity.Address.Address;
import com.example.demoe.Entity.Order.Order1;
import com.example.demoe.Entity.Order.Order1Item;
import com.example.demoe.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shipping {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
//    private BigDecimal priceShipping;
    private String status;
    private boolean isPayment;
//    private BigDecimal shippingFee;
//    private BigDecimal priceOrderItem;
    private LocalDateTime deliveryTime;
    private LocalDateTime shippingTime;
    private LocalDateTime receivedTime;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Object propertiesArray;

    @OneToMany(mappedBy = "shipping")
    private List<Order1Item> order1ItemList;

    public void addOrder1Item(Order1Item order1Item) {
       if(order1ItemList == null) {
           order1ItemList = new ArrayList<>();
       }
       order1ItemList.add(order1Item);
       order1Item.setShipping(this);
    }

    @ManyToOne
    @JoinColumn(name = "order1_id")
    @JsonIgnore
    private Order1 order1;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
