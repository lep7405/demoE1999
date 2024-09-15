package com.example.demoe.Entity.product;
import com.example.demoe.Entity.Admin;
import com.example.demoe.Entity.Order.Order1Item;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String productName;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String brand;
    private Boolean active;
    private Integer max1Buy;
    @ElementCollection
    private List<String> images = new ArrayList<>();
    @Transient
    private String productName_no_diacritics;
    @Transient
    private String productNameSearch;

//    @Type(JsonType.class)
//    @Column(columnDefinition = "json")
//    private List<Map<String, String>> propertiesArray;

    @ManyToMany(fetch= FetchType.EAGER)
    @JoinTable(
            name = "Product_Category",
            joinColumns = @JoinColumn(name = "Product_id"),
            inverseJoinColumns = @JoinColumn(name = "Cat_id")
    )
    private List<Category> categories;


    public void addCategory(Category category){
        if(categories==null){
            categories=new ArrayList<>();
        }
        categories.add(category);
        if(category.getProductList()==null){
            category.setProductList(new ArrayList<>());
        }
        category.getProductList().add(this);
    }
    @ManyToMany()
    @JoinTable(
            name = "Product_Discount",
            joinColumns = @JoinColumn(name = "Product_id"),
            inverseJoinColumns = @JoinColumn(name = "Discount_id")
    )
    private List<Discount> discounts;

    public void addDiscount(Discount discount){
        if(discounts==null){
            discounts=new ArrayList<>();
        }
        discounts.add(discount);
        if(discount.getProductList()==null){
            discount.setProductList(new ArrayList<>());
        }
        discount.getProductList().add(this);
    }


    @OneToMany(mappedBy = "product")
    private List<ProVar> proVarList;

    public void addProVar(ProVar proVar){
        if(proVarList==null){
            proVarList=new ArrayList<>();
        }
        proVarList.add(proVar);
        proVar.setProduct(this);
    }

    @ManyToOne
    @JoinColumn(name = "admin_id")
    @JsonIgnore
    private Admin admin;


    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL)
    private List<Rate> rateList;

    public void addRate(Rate rate){
        if(rateList==null){
            rateList=new ArrayList<>();
        }
        rateList.add(rate);
        rate.setProduct(this);
    }
    public int getRateCount() {
        return rateList != null ? rateList.size() : 0;
    }

    // Tính tổng rate trung bình
    public double getAverageRate() {
        if (rateList == null || rateList.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (Rate rate : rateList) {
            sum += rate.getRateNumber();
        }
        return sum / rateList.size();
    }

    @OneToOne(mappedBy = "product")
    private CountProduct countProduct;

    public void addCountProduct(CountProduct countProduct) {
        this.countProduct = countProduct;
        countProduct.setProduct(this);
    }

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<Order1Item> order1Items;

    public void addOrder1Item(Order1Item order1Item) {
        if(order1Items==null){
            order1Items=new ArrayList<>();
        }
        order1Items.add(order1Item);
        order1Item.setProduct(this);
    }

//    @OneToOne(mappedBy = "product")
//    @JsonIgnore
//    private CartItem cartItem;


    @OneToMany(mappedBy = "product")
    private List<Review> comments;
    public void addComment(Review comment){
        if(comments==null){
            comments=new ArrayList<>();
        }
        comments.add(comment);
        comment.setProduct(this);
    }
}
