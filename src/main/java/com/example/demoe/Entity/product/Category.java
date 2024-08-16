package com.example.demoe.Entity.product;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;// Đảm bảo tên cột trong cơ sở dữ liệu khớp với tên thuộc tính
    private String CategoryName;
    @ManyToOne
    @JoinColumn(name = "Parent_category_id")
    private Category parentCategory;

    @ManyToMany(mappedBy = "categories")
    @JsonIgnore
    private List<Product> productList;
}
