package com.example.demoe.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Service
public class JwtService1 {
    @Value("60")
    private long jwtExpiration;
    @Value("18000")
    private long refreshExpiration;
private static final String PUBLIC_KEY_PROPERTY = "rsa.public.key";
    private static final String PRIVATE_KEY_PROPERTY = "rsa.private.key";
    private final KeyPair keyPair;

    public JwtService1() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, FileNotFoundException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }

        String publicKeyString = properties.getProperty(PUBLIC_KEY_PROPERTY);
        String privateKeyString = properties.getProperty(PRIVATE_KEY_PROPERTY);

        if (publicKeyString != null && privateKeyString != null) {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
            this.keyPair = new KeyPair(
                    KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes)),
                    KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes))
            );
        } else {
            this.keyPair = generateRsaKeyPair();
            properties.setProperty(PUBLIC_KEY_PROPERTY, Base64.getEncoder().encodeToString(this.keyPair.getPublic().getEncoded()));
            properties.setProperty(PRIVATE_KEY_PROPERTY, Base64.getEncoder().encodeToString(this.keyPair.getPrivate().getEncoded()));
            try (FileOutputStream output = new FileOutputStream("src/main/resources/application.properties")) {
                properties.store(output, null);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Độ dài của khóa, ví dụ 2048 bit
        return keyPairGenerator.generateKeyPair();
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }
    public String buildToken(UserDetails userDetails,Map<String,Object> claims,long expiration) throws NoSuchAlgorithmException {
        if (claims == null) {
            claims = new HashMap<>();
        }
        long expiration1 = 60;
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(keyPair.getPrivate())
                .compact();
    }
    public String generateToken(UserDetails userDetails, Map<String,Object> claims) throws NoSuchAlgorithmException {
        return buildToken(userDetails,claims,(long) 60*1000*60*7);
    };
    public String generateRefreshToken(UserDetails userDetails) throws NoSuchAlgorithmException {
        return buildToken(userDetails,null,(long) 18000*1000*60);
    }
    //giải token
    public Claims extraToken(String token) throws NoSuchAlgorithmException {
        return Jwts.parserBuilder().setSigningKey(keyPair.getPublic()).build().parseClaimsJws(token).getBody();
    }
    public Claims extraRefreshToken(String token) throws NoSuchAlgorithmException {
        return Jwts.parserBuilder().setSigningKey(keyPair.getPublic()).build().parseClaimsJws(token).getBody();
    }
    public String getUserNameToken(String token) throws NoSuchAlgorithmException {
        return extraToken(token).getSubject();
    }
    public Date getExpirationToken(String token) throws NoSuchAlgorithmException {
        return extraToken(token).getExpiration();
    }
    public boolean isExpirationToken(String token) throws NoSuchAlgorithmException {
        return getExpirationToken(token).after(new Date());
    }

    public boolean isValidToken(UserDetails userDetails,String token) throws NoSuchAlgorithmException {
        return userDetails.getUsername().equals(getUserNameToken(token))&&isExpirationToken(token);
    }


    public String getUserNameRefreshToken(String refreshToken) throws NoSuchAlgorithmException {
        return extraRefreshToken(refreshToken).getSubject();
    }
    public Date getExpirationRefreshToken(String refreshToken) throws NoSuchAlgorithmException {
        return extraRefreshToken(refreshToken).getExpiration();
    }
    public boolean isExpirationRefreshToken(String refreshToken) throws NoSuchAlgorithmException {
        return getExpirationRefreshToken(refreshToken).after(new Date());
    }

    public boolean isValidRefreshToken(UserDetails userDetails,String refreshToken) throws NoSuchAlgorithmException {
        return userDetails.getUsername().equals(getUserNameRefreshToken(refreshToken))&&isExpirationRefreshToken(refreshToken);
    }

}
