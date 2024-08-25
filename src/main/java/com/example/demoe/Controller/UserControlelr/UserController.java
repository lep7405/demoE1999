package com.example.demoe.Controller.UserControlelr;

import com.example.demoe.Config.JwtService1;
import com.example.demoe.Dto.User.UserDTO;
import com.example.demoe.Dto.User.UserMDTO;
import com.example.demoe.Entity.ROLE.Role;
import com.example.demoe.Entity.TOKEN.Token;
import com.example.demoe.Entity.TOKEN.TokenType;
import com.example.demoe.Entity.User;
import com.example.demoe.Entity.cart.Cart;
import com.example.demoe.Helper.Singleton;
import com.example.demoe.Repository.AddressRepo;
import com.example.demoe.Repository.CartRepo;
import com.example.demoe.Repository.TokenRepository;
import com.example.demoe.Repository.UserRepo;
import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RestController
@RequestMapping("/sso")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class UserController {
    private Singleton singleton = Singleton.getInstance(null);
    Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserRepo userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AddressRepo addressRepo;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private JwtService1 jwtService1;
    @Autowired
    private UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/save/fingerprint/{id}")
    public String saveFingerprint(@PathVariable String id) throws IOException {
        System.out.println("hello1"+id);
        Singleton singleton1 = Singleton.getInstance(id);
        singleton1.setValue(id);
        System.out.println("hello2");
        System.out.println(singleton1.getValue());
        return singleton1.getValue();
    }
    @PostMapping("/Register")
    public ResponseEntity<UserMDTO> test8(@RequestBody User user) throws IOException {
        Optional<User> user1=userRepository.findByEmail(user.getEmail());
        if(user1.isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UserMDTO("email is exist"));
        }

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(new UserMDTO("password is null"));
        }
//        Address address=new Address();
//        addressRepo.save(address);
//        user.addAddress(address);
        user.setRole(Role.USER);




        User savedUser = userRepository.save(user);
        Cart cart = new Cart();
        cart.setUser(savedUser); // Gán user đã được lưu vào cart

        Cart savedCart = cartRepo.save(cart); // Lưu cart

// Nếu bạn có mối quan hệ hai chiều và muốn thiết lập lại tham chiếu từ user đến cart
        savedUser.setCart(savedCart);
        userRepository.save(savedUser);


        UserDTO userDTO=new UserDTO(savedUser.getId(),savedUser.getEmail());
        return ResponseEntity.ok(new UserMDTO("register successful",userDTO));
    }
    @PostMapping("/login")
    public ResponseEntity<userResData> login(@RequestBody User user) throws ServletException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        } catch (AuthenticationException ex) {
            // Xử lý lỗi xác thực (sai tên người dùng hoặc mật khẩu)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new userResData("Invalid username or password"));
        }

        // Xử lý thành công đăng nhập và tạo token
        Optional<User> userr = userRepository.findByEmail(user.getEmail());
        if(userr.isPresent()){
            User user1=userr.get();
            logger.info("users" + userr);
            String token;
            String refreshToken;
            try {
                token = jwtService1.generateToken(userr.get(), null);
                refreshToken=jwtService1.generateRefreshToken(userr.get());
            } catch (NoSuchAlgorithmException e) {
                throw new ServletException("Token generation failed", e);
            }
            revol(user1);
            Token tokenEntity = Token.builder()
                    .account(userr.get())
                    .token(token)
                    .tokenType(TokenType.BEARER)
                    .exprired(false)
                    .revolked(false)
                    .refreshToken(refreshToken)
                    .device( singleton.getValue())
                    .build();
            tokenRepository.save(tokenEntity);
            logger.info("2222222222222222222222222");
            user1.addToken(tokenEntity);
            logger.info("11111111111111111111111111111");



            return ResponseEntity.ok(new userResData(user1.getId(),user1.getEmail(),tokenEntity.getToken(),tokenEntity.getRefreshToken(),"loggin successful"));
        }
        else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new userResData("Invalid username or password"));
        }
    }


    @GetMapping("/getUser/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user1 = user.get();
        UserDTO userDTO = new UserDTO(user1.getId(), user1.getEmail());
        return ResponseEntity.ok(user1);
    }

    private void revol(User user){
        List<Token> tokenList=tokenRepository.findAllByUserId(user.getId(),singleton.getValue());
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


    @PostMapping("/refreshToken")
    public ResponseEntity<userResData> refreshToken( @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) throws NoSuchAlgorithmException {
        final String token=authHeader.substring(7);
        String emaill = jwtService1.getUserNameRefreshToken(token);
        Optional<User> userr = userRepository.findByEmail(emaill);
        var isTokenValid = tokenRepository.findByToken(token)
                .map(t -> !t.isRevolked() && !t.isRevolked())
                .orElse(false);
        UserDetails userDetails=userDetailsService.loadUserByUsername(emaill);
            if(jwtService1.isValidRefreshToken(userDetails,token)&&isTokenValid){
                if(jwtService1.isExpirationRefreshToken(token)){
                   String token1 = jwtService1.generateToken(userr.get(), null);
                    String refreshToken=jwtService1.generateRefreshToken(userr.get());
                    return ResponseEntity.ok(new userResData(token1,refreshToken));
                }
                else {
                    return ResponseEntity.ok(new userResData("refresh token is not expired"));
                }
            }
                else{
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new userResData("refresh token is not valid"));
                }

    }

//    @PostMapping("/refreshToken")
//    public String refreshToken(@RequestBody Token token) throws NoSuchAlgorithmException {
//
//    }
}

//logout
//refresh token
//login check token thì phải check lại token
//khi muốn logout ra tất cả tài khoản thì phải thế nào ,chắc bắt nhập lại mật khẩu à, logout hết tài khảon thì phải xóa đi hết tất cả token à













//@GetMapping("/loginSuccess")
//public ResponseEntity<UserMDTO> loginSuccess(@AuthenticationPrincipal OAuth2User oauth2User) throws NoSuchAlgorithmException {
//    // Lấy thông tin người dùng từ principal OAuth2User
//    String email = oauth2User.getAttribute("email");
//    System.out.println(oauth2User.getAttributes());
//    // Hiển thị thông tin người dùng
//    Optional<User> user1=userRepository.findByEmail(email);
//    if(user1.isPresent()){
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UserMDTO("email is exist"));
//    }
//    Address address=new Address();
//    addressRepo.save(address);
//    User user=new User();
//    user.setEmail(email);
//    user.setRole(Role.USER);
//    user.addAddress(address);
//    user.setRole(Role.USER);
//
//    User savedUser = userRepository.save(user);
//
//    var token0=jwtService1.generateToken(savedUser,null);
//    System.out.println(token0);
//    revol(savedUser);
//    var token1 = Token.builder()
//            .account(savedUser)
//            .token(token0)
//            .tokenType(TokenType.BEARER)
//            .exprired(false)
//            .revolked(false)
//            .build();
//    tokenRepository.save(token1);
//    savedUser.addToken(token1);
//    UserDTO userDTO=new UserDTO(savedUser.getId(),savedUser.getEmail(),token0);
//    return ResponseEntity.ok(new UserMDTO("register successful",userDTO));
//}
//
