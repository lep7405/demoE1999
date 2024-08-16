package com.example.demoe.Dto.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String password;
    private String phone;
    private String token;
    private String refreshToken;
    public UserDTO(Long id,String email,String token,String refreshToken){
        this.id = id;
        this.email = email;
        this.token = token;
        this.refreshToken = refreshToken;
    }
    public UserDTO(Long id,String email){
        this.id = id;
        this.email = email;
    }
    public UserDTO(Long id,String email,String token){
        this.id = id;
        this.email = email;
        this.token = token;
    }
}
