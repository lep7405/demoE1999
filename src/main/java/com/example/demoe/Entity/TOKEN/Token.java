package com.example.demoe.Entity.TOKEN;

import com.example.demoe.Entity.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.w3c.dom.Text;

@Entity
@Getter
@Setter
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
    @JoinColumn(name = "account_id")
    @JsonIgnore
    private Account account;

}
