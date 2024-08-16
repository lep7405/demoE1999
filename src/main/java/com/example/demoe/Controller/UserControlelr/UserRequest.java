package com.example.demoe.Controller.UserControlelr;

import com.example.demoe.Entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private User user;
    private String deviceId;
}
