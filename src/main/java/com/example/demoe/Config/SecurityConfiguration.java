
package com.example.demoe.Config;

import com.example.demoe.Dto.User.UserDTO;
import com.example.demoe.Entity.Address.Address;
import com.example.demoe.Entity.ROLE.Role;
import com.example.demoe.Entity.TOKEN.Token;
import com.example.demoe.Entity.TOKEN.TokenType;
import com.example.demoe.Entity.User;
import com.example.demoe.Helper.Singleton;
import com.example.demoe.Repository.AddressRepo;
import com.example.demoe.Repository.TokenRepository;
import com.example.demoe.Repository.UserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity

public class SecurityConfiguration {

    private final JwtAuthFilter1 jwtAuthFilter1;
    private Singleton singleton = Singleton.getInstance(null);
    @Autowired
    private UserRepo userRepository;

    @Autowired
    private JwtService1 jwtService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AddressRepo addressRepo;
    private static final String[] Lists = {
            "/sso/Register",
            "/sso/getUser/**",
            "/sso/**",
            "/login/oauth2/code/google",
            "/sso/login",
            "/login", "/oauth2/**",
            "/product/getProduct/**",
            "/product/getAllProduct",
            "/admin/login",
            "/product/test-redis",
            "/product/testPro/**",
            "/product/**",
            "/cart/clear",
            "/payment/**",

            "/order/getOrder1/**",
            "/order/getOrder1Item/**"

    };
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(req ->
                        req.requestMatchers(Lists).permitAll()
                                .requestMatchers("/product/test","/product/testInput").hasAnyAuthority("admin:create")
                                .anyRequest().authenticated()

                )
                .oauth2Login(oauth2->oauth2
                                .loginPage("/oauth2/authorization/google")
                                .clientRegistrationRepository(clientRegistrationRepository())
//                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService()))
                                .successHandler(this::onAuthenticationSuccess)
                                .failureHandler(this::onAuthenticationFailure)
                )


                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter1, UsernamePasswordAuthenticationFilter.class)

        ;
        return http.build();
    }
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(this.googleClientRegistration());
    }

    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId("621355433281-em1qb0aa7jcii3gueo1jpmhghc1qc7la.apps.googleusercontent.com")
                .clientSecret("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")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8090/login/oauth2/code/google")
                .scope("openid", "profile", "email", "address", "phone")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                .clientName("Google")
                .build();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return new DefaultOAuth2UserService() {
            @Override
            public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
                OAuth2User user = super.loadUser(userRequest);
                // Customize user attributes if needed
                return user;
            }
        };
    }

    private void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        // Hiển thị thông tin người dùng
        Optional<User> user1 = userRepository.findByEmail(email);
        if (user1.isPresent()) {
            String token;
            try {
                token = jwtService.generateToken(user1.get(), null);
            } catch (NoSuchAlgorithmException e) {
                throw new ServletException("Token generation failed", e);
            }
            revol(user1.get(),singleton.getValue());
            Token tokenEntity = Token.builder()
                    .account(user1.get())
                    .token(token)
                    .tokenType(TokenType.BEARER)
                    .exprired(false)
                    .revolked(false)
                    .device( singleton.getValue())
                    .build();
            tokenRepository.save(tokenEntity);
            user1.get().addToken(tokenEntity);
            ObjectMapper objectMapper = new ObjectMapper();
            UserDTO userDTO=new UserDTO(user1.get().getId(),user1.get().getEmail(),token);
            String userDtoJson = objectMapper.writeValueAsString(userDTO);

            // Mã hóa chuỗi JSON bằng Base64
            String encodedUserDtoJson = Base64.getEncoder().encodeToString(userDtoJson.getBytes());

            // Thiết lập cookie với chuỗi JSON mã hóa
            Cookie cookie = new Cookie("userDTO", encodedUserDtoJson);
//            cookie.setHttpOnly(true); // Đảm bảo cookie chỉ có thể được truy cập bởi server
//            cookie.setSecure(true); // Đảm bảo cookie chỉ được gửi qua HTTPS
            cookie.setPath("/"); // Đảm bảo cookie có sẵn cho toàn bộ ứng dụng
            cookie.setMaxAge(3600); // Đặt thời gian sống của cookie (tính bằng giây)
            response.addCookie(cookie);

            // Redirect đến front-end mà không có token trong URL
            String redirectUrl = "http://localhost:5173/";
            response.sendRedirect(redirectUrl);
            return;
        }

        Address address = new Address();
        addressRepo.save(address);

        User user = new User();
        user.setEmail(email);
        user.addAddress(address);
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);
        revol(savedUser,singleton.getValue());
        String token;
        try {
            token = jwtService.generateToken(savedUser, null);
        } catch (NoSuchAlgorithmException e) {
            throw new ServletException("Token generation failed", e);
        }

        Token tokenEntity = Token.builder()
                .account(savedUser)
                .token(token)
                .tokenType(TokenType.BEARER)
                .exprired(false)
                .revolked(false)
                .device( singleton.getValue())
                .build();
        tokenRepository.save(tokenEntity);
        savedUser.addToken(tokenEntity);
        userRepository.save(savedUser);
        ObjectMapper objectMapper = new ObjectMapper();
        UserDTO userDTO=new UserDTO(savedUser.getId(),savedUser.getEmail(),token);
        String userDtoJson = objectMapper.writeValueAsString(userDTO);

        // Mã hóa chuỗi JSON bằng Base64
        String encodedUserDtoJson = Base64.getEncoder().encodeToString(userDtoJson.getBytes());

        // Thiết lập cookie với chuỗi JSON mã hóa
        Cookie cookie = new Cookie("userDTO", encodedUserDtoJson);
        // Đảm bảo cookie chỉ được gửi qua HTTPS
        cookie.setPath("/"); // Đảm bảo cookie có sẵn cho toàn bộ ứng dụng
        cookie.setMaxAge(3600); // Đặt thời gian sống của cookie (tính bằng giây)
        response.addCookie(cookie);

        // Redirect đến front-end mà không có token trong URL
        String redirectUrl = "http://localhost:5173/";
        response.sendRedirect(redirectUrl);
    }
    private void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException  exception) throws IOException {

        // Redirect đến front-end mà không có token trong URL
        String redirectUrl = "http://localhost:5173/register";
        response.sendRedirect(redirectUrl);
    }
    private void revol(User user,String deivce) {
        List<Token> tokenList=tokenRepository.findAllByUserId(user.getId(),deivce);
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
