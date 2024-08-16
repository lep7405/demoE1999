package com.example.demoe.Entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Var {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String key1;
    private String value;

    @ManyToOne
    @JoinColumn(name = "provar_id")
    @JsonIgnore
    private ProVar proVar;
}
