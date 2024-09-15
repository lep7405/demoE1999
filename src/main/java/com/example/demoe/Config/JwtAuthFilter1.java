package com.example.demoe.Config;

import com.example.demoe.Entity.TOKEN.Token;
import com.example.demoe.Repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter1 extends OncePerRequestFilter {
    private final JwtService1 jwtService1;
    private final UserDetailsService userDetailsService;
    @Autowired
    private final TokenRepository tokenRepo;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader=request.getHeader("Authorization");
        if(authHeader==null||!authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return ;
        }
        final String jwt=authHeader.substring(7);
        final String emaill;
        try {
            emaill = jwtService1.getUserNameToken(jwt);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        Optional<Token> optionalToken = tokenRepo.findByToken(jwt);
        if(!optionalToken.isPresent()){
            filterChain.doFilter(request,response);
            return;
        }
        Token token = optionalToken.get();

        String finger=token.getDevice();
        if(emaill!=null&& SecurityContextHolder.getContext().getAuthentication()==null){
            UserDetails userDetails=this.userDetailsService.loadUserByUsername(emaill);
            var isTokenValid = tokenRepo.findByToken(jwt)
                    .map(t -> !t.isRevolked() && !t.isRevolked()&&t.getDevice().equals(finger))
                    .orElse(false);
            try {
                if(jwtService1.isValidToken(userDetails,jwt)&&isTokenValid){
                    if(jwtService1.isExpirationToken(jwt)){
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        filterChain.doFilter(request,response);
    }
}