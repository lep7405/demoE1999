package com.example.demoe.Controller.UserControlelr;

import com.example.demoe.Config.JwtService1;
import com.example.demoe.Dto.ReviewDto;
import com.example.demoe.Dto.User.UserDTO;
import com.example.demoe.Dto.User.UserMDTO;
import com.example.demoe.Entity.ROLE.Role;
import com.example.demoe.Entity.TOKEN.Token;
import com.example.demoe.Entity.TOKEN.TokenType;
import com.example.demoe.Entity.User;
import com.example.demoe.Entity.cart.Cart;
import com.example.demoe.Entity.product.Review;
import com.example.demoe.Helper.Singleton;
import com.example.demoe.Repository.*;
import com.example.demoe.Service.S3Service;
import com.example.demoe.Service.UserService;
import jakarta.servlet.ServletException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RestController
@RequestMapping("/user")
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
    private TokenRepository tokenRepository;
    @Autowired
    private ReviewRepo reviewRepo;
    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private JwtService1 jwtService1;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private UserService userService;
    @Autowired
    private S3Service s3Service;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/Register")
    public ResponseEntity<UserMDTO> test8(@Valid @RequestBody User user) throws IOException {
        Optional<User> user1 = userRepository.findByEmail(user.getEmail());


        if(user1.isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UserMDTO("email is exist"));
        }

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(new UserMDTO("password is null"));
        }
        user.setRole(Role.USER);
        User savedUser = userRepository.save(user);
        Cart cart = new Cart();
        cart.setUser(savedUser);
        Cart savedCart = cartRepo.save(cart);
        savedUser.setCart(savedCart);
        userRepository.save(savedUser);
        UserDTO userDTO=new UserDTO(savedUser.getId(),savedUser.getEmail());
        return ResponseEntity.ok(new UserMDTO("register successful",userDTO));
    }
    @PostMapping("/Login")
    public ResponseEntity<UserMDTO> login(@RequestBody UserRequest userRequest) throws ServletException {
        User user=userRequest.getUser();
        String deviceId=userRequest.getDeviceId();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UserMDTO("Invalid username or password"));
        }
        Optional<User> user1 = userRepository.findByEmail(user.getEmail()) ;
        if(!user1.isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UserMDTO("user not found"));
        }
            String token;
            String refreshToken;
            try {
                token = jwtService1.generateToken(user1.get(), null);
                refreshToken=jwtService1.generateRefreshToken(user1.get());
            } catch (NoSuchAlgorithmException e) {
                throw new ServletException("Token generation failed", e);
            }
            revol(user1.get(),deviceId);
            Token tokenEntity = Token.builder()
                    .account(user1.get())
                    .token(token)
                    .tokenType(TokenType.BEARER)
                    .exprired(false)
                    .revolked(false)
                    .refreshToken(refreshToken)
                    .device(deviceId)
                    .build();
            tokenRepository.save(tokenEntity);
            user1.get().addToken(tokenEntity);
            return ResponseEntity.ok(new UserMDTO("loggin successful",new UserDTO(user1.get().getId(),user1.get().getEmail(),tokenEntity.getToken(),tokenEntity.getRefreshToken())));
        }

    @GetMapping("/getUser")
    public ResponseEntity<User> getUser() {
        Optional<User> user = userService.getAuthenticatedUser();
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User user1 = user.get();
        UserDTO userDTO = new UserDTO(user1.getId(), user1.getEmail());
        return ResponseEntity.ok(user1);
    }


    @GetMapping("/refreshToken")
    public ResponseEntity<UserMDTO> refreshToken( @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) throws NoSuchAlgorithmException {
        final String token=authHeader.substring(7);
        String emaill = jwtService1.getUserNameRefreshToken(token);
        Optional<User> userr = userRepository.findByEmail(emaill);
        if(!userr.isPresent()){

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UserMDTO("user is not exist"));
        }

        Optional<Token> token2 = tokenRepository.findByRefreshToken(token);
        if(!token2.isPresent()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UserMDTO("Token is not exist"));
        }
        String finger=token2.get().getDevice();

        var isRefreshTokenValid = tokenRepository.findByRefreshToken(token)
                .map(t -> !t.isRevolked() && !t.isRevolked()&&t.getDevice().equals(finger))
                .orElse(false);
        if(!isRefreshTokenValid){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UserMDTO("refresh token is not valid"));
        }
        UserDetails userDetails=userDetailsService.loadUserByUsername(emaill);
            if(jwtService1.isValidRefreshToken(userDetails,token)&&isRefreshTokenValid){
                if(jwtService1.isExpirationRefreshToken(token)){
                    revol(userr.get(),finger);
                   String token1 = jwtService1.generateToken(userr.get(), null);
                    String refreshToken1=jwtService1.generateRefreshToken(userr.get());
                    Token tokenEntity = Token.builder()
                            .account(userr.get())
                            .token(token1)
                            .tokenType(TokenType.BEARER)
                            .exprired(false)
                            .revolked(false)
                            .refreshToken(refreshToken1)
                            .device(finger)
                            .build();
                    tokenRepository.save(tokenEntity);
                    return ResponseEntity.ok(new UserMDTO("success",new UserDTO(token1,refreshToken1)));
                }
                else {
                    return ResponseEntity.ok(new UserMDTO("refresh token is not expired"));
                }
            }
                else{
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UserMDTO("refresh token is not valid"));
                }

    }

    @GetMapping("/getAllReview")
    public ResponseEntity<List<ReviewDto>> getAllReview() {
        // Đo thời gian tổng quát
        long startTime = System.currentTimeMillis();

        // Bước 1: Lấy thông tin người dùng
        long step1Start = System.currentTimeMillis();
        User user = userService.getAuthenticatedUser().get();
        long step1End = System.currentTimeMillis();
        System.out.println("Thời gian lấy thông tin người dùng: " + (step1End - step1Start) + "ms");

        // Bước 2: Truy vấn danh sách review của người dùng
        long step2Start = System.currentTimeMillis();
        List<Review> reviewList = reviewRepo.getListCommentByUserId(user.getId());
        long step2End = System.currentTimeMillis();
        System.out.println("Thời gian truy vấn danh sách review: " + (step2End - step2Start) + "ms");

        // Bước 3: Chuyển đổi reviewList thành reviewDtoList
        long step3Start = System.currentTimeMillis();
        List<ReviewDto> reviewDtoList = new ArrayList<>();
        for (Review review : reviewList) {
            long reviewStart = System.currentTimeMillis(); // Đo thời gian từng vòng lặp
            ReviewDto reviewDto = ReviewDto.builder()
                    .id(review.getId())
                    .commentTime(review.getCommentTime())
                    .content(review.getContent())
                    .contextImage(review.getContextImage())
                    .productName(review.getProduct().getProductName())
                    .productId(review.getProduct().getId())
                    .productImage(s3Service.getPresignedUrl(review.getProduct().getImages().get(0)))
                    .rateNumber(review.getRateNumber())
                    .build();
            reviewDtoList.add(reviewDto);
            long reviewEnd = System.currentTimeMillis();
            System.out.println("Thời gian xử lý review ID " + review.getId() + ": " + (reviewEnd - reviewStart) + "ms");
        }
        long step3End = System.currentTimeMillis();
        System.out.println("Thời gian xử lý và chuyển đổi các review: " + (step3End - step3Start) + "ms");

        // Bước 4: Trả về kết quả
        long step4Start = System.currentTimeMillis();
        ResponseEntity<List<ReviewDto>> response = ResponseEntity.ok(reviewDtoList);
        long step4End = System.currentTimeMillis();
        System.out.println("Thời gian trả về ResponseEntity: " + (step4End - step4Start) + "ms");

        // Tổng thời gian
        long endTime = System.currentTimeMillis();
        System.out.println("Tổng thời gian thực thi: " + (endTime - startTime) + "ms");

        return response;
    }
    private void revol(User user,String deviceId){
        List<Token> tokenList=tokenRepository.findAllByUserId(user.getId(),deviceId);
        if(tokenList==null){
            return;
        }
        tokenList.forEach(token -> {
            token.setRevolked(true);
            token.setExprired(true);
        });
        tokenRepository.saveAll(tokenList);
    }
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


//    @CrossOrigin(origins = "http://localhost:5173")
//    @GetMapping("/save/fingerprint/{id}")
//    public String saveFingerprint(@PathVariable String id) throws IOException {
//        System.out.println("hello1"+id);
//        Singleton singleton1 = Singleton.getInstance(id);
//        singleton1.setValue(id);
//        System.out.println("hello2");
//        System.out.println(singleton1.getValue());
//        return singleton1.getValue();
//    }

//@GetMapping("/test-singleton")
//public String test(){
//    return singleton.getValue();
//}