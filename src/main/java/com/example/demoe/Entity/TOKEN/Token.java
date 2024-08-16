package com.example.demoe.Entity.TOKEN;

import com.example.demoe.Entity.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.w3c.dom.Text;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "TEXT")
    private String token;
    private TokenType tokenType=TokenType.BEARER;
    private boolean revolked;
    private boolean exprired;
    @Column(columnDefinition = "TEXT")
    private String refreshToken;
    private String device;
    @ManyToOne()
    @JsonIgnore
    private Account account;

}
