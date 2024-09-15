package com.example.demoe.Controller.Admin;

import com.example.demoe.Entity.Admin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminRequest {
    private Admin admin;
    private String deviceId;
}
