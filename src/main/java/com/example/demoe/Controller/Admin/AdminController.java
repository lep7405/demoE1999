package com.example.demoe.Controller.Admin;

import com.example.demoe.Config.JwtService1;
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
    private Singleton singleton = Singleton.getInstance(null);
    @Autowired
    private JwtService1 jwtService1;
    @Autowired
    private AdminRepo adminRepo;
    @Autowired
    private TokenRepository tokenRepository;
    @PostMapping("/login")
    public ResponseEntity<userResData> login(@RequestBody Admin admin) throws ServletException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(admin.getEmail(), admin.getPassword()));
        } catch (AuthenticationException ex) {
            // Xử lý lỗi xác thực (sai tên người dùng hoặc mật khẩu)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new userResData("Invalid username or password"));
        }

        // Xử lý thành công đăng nhập và tạo token
        Optional<Admin> admin1 = adminRepo.findByEmail(admin.getEmail());
        if(admin1.isPresent()){

            String token;
            String refreshToken;
            try {
                token = jwtService1.generateToken(admin1.get(), null);
                refreshToken=jwtService1.generateRefreshToken(admin1.get());
            } catch (NoSuchAlgorithmException e) {
                throw new ServletException("Token generation failed", e);
            }
            revol(admin1.get());
            Token tokenEntity = Token.builder()
                    .account(admin1.get())
                    .token(token)
                    .tokenType(TokenType.BEARER)
                    .exprired(false)
                    .revolked(false)
                    .refreshToken(refreshToken)
                    .device( singleton.getValue())
                    .build();
            tokenRepository.save(tokenEntity);

            admin1.get().addToken(tokenEntity);

            return ResponseEntity.ok(new userResData(admin1.get().getId(),admin1.get().getEmail(),tokenEntity.getToken(),tokenEntity.getRefreshToken(),"loggin successful"));
        }
        else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new userResData("Invalid username or password"));
        }
    }

    private void revol(Admin admin){
        List<Token> tokenList=tokenRepository.findAllByUserId(admin.getId(),singleton.getValue());
        if(tokenList==null){
            return;
        }
        tokenList.forEach(token -> {
            token.setRevolked(true);
            token.setExprired(true);
        });
        tokenRepository.saveAll(tokenList);
    }
    @GetMapping("/test-singleton")
    public String test(){
        return singleton.getValue();
    }

    @GetMapping("/getInfoAdmin")
    public Admin getInfoAdmin(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        Admin admin = (Admin) authentication.getPrincipal();
        String email=admin.getEmail();
        Optional<Admin> admin1=adminRepo.findByEmail(email);

        return admin1.get();
    }
}
