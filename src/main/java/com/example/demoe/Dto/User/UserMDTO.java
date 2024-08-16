package com.example.demoe.Dto.User;

import com.example.demoe.Dto.User.UserDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMDTO {
    private String message;
    private UserDTO userDTO;
    public UserMDTO(String message,UserDTO userDTO){
        this.message = message;
        this.userDTO = userDTO;
    }
    public UserMDTO(String message){
        this.message = message;
    }
    public UserMDTO(){

    }
}
