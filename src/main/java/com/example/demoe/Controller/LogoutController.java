package com.example.demoe.Controller;


import com.example.demoe.Entity.Admin;
import com.example.demoe.Entity.TOKEN.Token;
import com.example.demoe.Entity.User;
import com.example.demoe.Helper.Singleton;
import com.example.demoe.Repository.AdminRepo;
import com.example.demoe.Repository.TokenRepository;
import com.example.demoe.Repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/logout")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class LogoutController {
    Logger logger = LoggerFactory.getLogger(LogoutController.class);
    private Singleton singleton = Singleton.getInstance(null);
    @Autowired
    private UserRepo userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private AdminRepo adminRepo;
    @GetMapping("out")
    public ResponseEntity<String> logout(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication!=null){
            logger.info("authentication khac null");
        }
        String name = authentication.getName();

        logger.info("11111111111111111111"+name);
//        String username = user.getUsername();
        Optional<User> user1=userRepository.findByEmail(name);
        Optional<Admin> admin1=adminRepo.findByEmail(name);
        if (user1.isPresent()) {
            User user = user1.get();
            List<Token> userTokens = tokenRepository.findAllByUserId(user.getId(),singleton.getValue());
            userTokens.forEach(token -> {
                token.setRevolked(true);
                token.setExprired(true);
            });
            tokenRepository.saveAll(userTokens);
        } else {
            if(admin1.isPresent()){
                Admin admin = admin1.get();
                List<Token> adminTokens = tokenRepository.findAllByUserId(admin.getId(),singleton.getValue());
                adminTokens.forEach(token -> {
                    token.setRevolked(true);
                    token.setExprired(true);
                });
                tokenRepository.saveAll(adminTokens);
            }
            else{
                logger.warn("User or seller not found: {}", name);
            }
        }
        return ResponseEntity.ok("Logout successful");
    }
}
