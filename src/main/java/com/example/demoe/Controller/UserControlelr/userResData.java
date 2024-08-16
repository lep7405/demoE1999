package com.example.demoe.Controller.UserControlelr;
import lombok.Data;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Data
public class userResData {
    private Long id;
    private String email;
    private String token;
    private String refreshToken;
    private String textResponse;


    public userResData(Long id, String email, String token,String refreshToken, String textResponse){
        this.id = id;
        this.email = email;
        this.token = token;
        this.refreshToken=refreshToken;
        this.textResponse = textResponse;
    }
    public userResData(String textResponse){
        this.textResponse=textResponse;
    }
    public userResData(String token, String refreshToken){
        this.token=token;
        this.refreshToken=refreshToken;
    }
    public userResData(){

    }
}
