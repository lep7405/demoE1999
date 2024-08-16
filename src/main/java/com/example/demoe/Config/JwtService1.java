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
    @Value("${application.security.jwt.expiration}00")
    private long jwtExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}000")
    private long refreshExpiration;
//    @Value("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA564/5dxnmKt2FIsOYVbbTj5gM8I2mn7nqf1NoB0TAYP9/fpaeaOJT146r2LTyk1RpBzfDSp3Ya0ilARzS/V5/Ty1b+IdNAOarWAW6VKHiGR9VQtKNfnoVZ7Sloqj5MM8G3Vdrlzt9HVbHJJD0qgs56ZpIdqVQL+fOl+EQPLV+FZ9SRShv+cm7yMatPZWV2g8SFyDK2jQma4oiMBP1g8gVaBkoRZjMs0JvGIUwk4OUsjNht7jg+gvF9ayJ0orHuFaK+DBcb9zTayv5MJEFJWE9Zdsv8di1jcUvRboIcmReEx48GTzoPu3zfu3ePcH6LmTy2FphVknAHhwrJUbF9EVuQIDAQAB")
//    private String publicKey;
//    @Value("MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDnrj/l3GeYq3YUiw5hVttOPmAzwjaafuep/U2gHRMBg/39+lp5o4lPXjqvYtPKTVGkHN8NKndhrSKUBHNL9Xn9PLVv4h00A5qtYBbpUoeIZH1VC0o1+ehVntKWiqPkwzwbdV2uXO30dVsckkPSqCznpmkh2pVAv586X4RA8tX4Vn1JFKG/5ybvIxq09lZXaDxIXIMraNCZriiIwE/WDyBVoGShFmMyzQm8YhTCTg5SyM2G3uOD6C8X1rInSise4Vor4MFxv3NNrK/kwkQUlYT1l2y/x2LWNxS9FughyZF4THjwZPOg+7fN+7d49wfouZPLYWmFWScAeHCslRsX0RW5AgMBAAECggEAIiGqqjuBS52fBMHDuZVpM6q+04E+OB6QFJwLrpX58dQ3PA6A+8Ca/wVb0fDWUMx5RZs3RSJHei1elb3eGoeslCE4faYrNTl+tSId3fvZmb9sM3Y5VaBemRaP4bvFD3OvzD/LMF/icoxDgv8NXOCH1QzQGjuwl1xjod7mhgcAfNhBU7f8w0zUQReUrDT8aiTY34YB4JNFxqbMMisjYwzIGgF190x0CyWGEyhMgNKhEkXoFj8fzJUdTrAXrqdjNiDbC2QSRwmG6CaWa+NQZn6QcmeCQZU6syZeHGJv6OUt+3Puna+8a9tMTxUvvNYFr2T0U06qT3BFD7dM+W586WEw7QKBgQDwu7hUm/RHNeWUpcnbnGOQstiBXbUrMy0Es+eJa+2Z9Tb0Or8DhhXHMgcoLf+aZCRtslBec507qEU5XNMYxCEXCA6NEcbYH3yJzQGzfciOrh1N+MZBXPtDsdzJzGxWw7tCTf42cwcIFiZdEtemx6ltPID6WTnamvZ0w+lY6mNNJwKBgQD2X4+slf/gwn+UAvxrDo8u0oC1Ziha0P/Agg8QA7n7Et4oZF47YvaZCkChaCEdjLlCJ/45qdePxaasgFrB6+rktt5vyqUqa/RQ+yZY6cQpIqYyXozGyHU9mhLyyZN0izTcVdE+9BuooOlZmlYlJEfrKHoY8Y/yRfIR8yHLCXISHwKBgQCynIUyeZev8iRJuwxfHdSduqR6r31I9Sp0r8qdom9i6JaCdMfepHCqH8tBm4dnFOAWZ9PLkHpblgjue5nAaVynyvmI32SqXAo579pQQ4Y3nqXtgSQPwR5IZsILE9paMdVRZ66y1XulG1IRgnZwrvldLCQCG4uaT5mGnwkWSKmuxwKBgH+hy81tu6wVNoBX96oCVdy8wl62+IrroMJ63hC+zXcrNBmavVJolzV7ITY8uoMTZtnF3CbFPN3HV+wfZmT1sa8gqSUixuOuH1hgc2I2YB+rSovnfWhxaoMqMUzDbnc+sni+oHLLhRSbn6eCk8QUAonpYy9O3PJ2l8wCt2v2jbijAoGAB7yF0r2nKuTQaFkPrVFdAHUYHej5IYVbcyP48cuj9wuFkxQuKzAGvZTa4PCzCaxfp3+T+ABN2sokeA9XEG5IjTCzu6rHGi5QBId0W/907DORStuA1KOAl6q3NjuQWP9t7pEa7Tn5jywj9ZRu6a27r3+pm+qg/2qyHHVMbri0N38=")
//    private String secretKey;
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

        System.out.println("Public Key: " + Base64.getEncoder().encodeToString(this.keyPair.getPublic().getEncoded()));
        System.out.println("Private Key: " + Base64.getEncoder().encodeToString(this.keyPair.getPrivate().getEncoded()));
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
            // Xử lý trường hợp tham số claims là null tại đây, ví dụ:
            claims = new HashMap<>(); // hoặc bất kỳ xử lý nào khác
        }
        System.out.println("45"+keyPair.getPrivate());
        System.out.println("46"+keyPair.getPublic());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(keyPair.getPrivate())
                .compact();
    }
    public String generateToken(UserDetails userDetails, Map<String,Object> claims) throws NoSuchAlgorithmException {
        return buildToken(userDetails,claims,jwtExpiration);
    };
    public String generateRefreshToken(UserDetails userDetails) throws NoSuchAlgorithmException {
        return buildToken(userDetails,null,refreshExpiration);
    }
    //giải token
    public Claims extraToken(String token) throws NoSuchAlgorithmException {
        System.out.println("63"+keyPair.getPrivate());
        System.out.println("64"+keyPair.getPublic());
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
