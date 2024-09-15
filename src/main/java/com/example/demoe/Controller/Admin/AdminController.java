package com.example.demoe.Controller.Admin;

import com.example.demoe.Config.JwtService1;
import com.example.demoe.Controller.UserControlelr.UserRequest;
import com.example.demoe.Controller.UserControlelr.userResData;
import com.example.demoe.Dto.Product.ProductDto;
import com.example.demoe.Entity.Admin;
import com.example.demoe.Entity.TOKEN.Token;
import com.example.demoe.Entity.TOKEN.TokenType;
import com.example.demoe.Entity.User;
import com.example.demoe.Helper.Singleton;
import com.example.demoe.Repository.AdminRepo;
import com.example.demoe.Repository.TokenRepository;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin(origins ={"http://localhost:5173", "http://localhost:5174"})
public class AdminController {
    private final AuthenticationManager authenticationManager;
    @Autowired
    private JwtService1 jwtService1;
    @Autowired
    private AdminRepo adminRepo;
    @Autowired
    private TokenRepository tokenRepository;
    @PostMapping("/Login")
    public ResponseEntity<userResData> login(@RequestBody AdminRequest adminRequest) throws ServletException {
        System.out.println("hello1");
        Admin admin=adminRequest.getAdmin();
        String deviceId=adminRequest.getDeviceId();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(admin.getEmail(), admin.getPassword()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new userResData("Invalid username or password"));
        }
        Admin admin1 = adminRepo.findByEmail(admin.getEmail()) .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + admin.getEmail()));
        String token;
        String refreshToken;
        try {
            token = jwtService1.generateToken(admin1, null);
            refreshToken=jwtService1.generateRefreshToken(admin1);
        } catch (NoSuchAlgorithmException e) {
            throw new ServletException("Token generation failed", e);
        }
        revol(admin1,deviceId);
        Token tokenEntity = Token.builder()
                .account(admin1)
                .token(token)
                .tokenType(TokenType.BEARER)
                .exprired(false)
                .revolked(false)
                .refreshToken(refreshToken)
                .device(deviceId)
                .build();
        tokenRepository.save(tokenEntity);
        admin1.addToken(tokenEntity);
        return ResponseEntity.ok(new userResData(admin1.getId(),admin1.getEmail(),tokenEntity.getToken(),tokenEntity.getRefreshToken(),"loggin successful"));
    }

    private void revol(Admin admin,String deviceId){
        List<Token> tokenList=tokenRepository.findAllByUserId(admin.getId(),deviceId);
        if(tokenList==null){
            return;
        }
        tokenList.forEach(token -> {
            token.setRevolked(true);
            token.setExprired(true);
        });
        tokenRepository.saveAll(tokenList);
    }

    @GetMapping("/getInfoAdmin")
    public Admin getInfoAdmin(){
        Optional<Admin> admin1=adminRepo.findById(2L);

        return admin1.get();
    }
}
