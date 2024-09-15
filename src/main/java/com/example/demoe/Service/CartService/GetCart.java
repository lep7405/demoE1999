package com.example.demoe.Service.CartService;

import com.example.demoe.Dto.Cart.CartDto;
import com.example.demoe.Dto.Cart.CartDtoMessage;
import com.example.demoe.Dto.Cart.CartItemDto;
import com.example.demoe.Dto.Cart.Vars;
import com.example.demoe.Dto.Discount.DiscountDtoCartRedis;
import com.example.demoe.Entity.User;
import com.example.demoe.Entity.cart.Cart;
import com.example.demoe.Entity.cart.CartItem;
import com.example.demoe.Entity.product.Discount;
import com.example.demoe.Helper.JedisSingleton;
import com.example.demoe.Repository.CartItemRepo;
import com.example.demoe.Repository.CartRepo;
import com.example.demoe.Repository.DiscountRepo;
import com.example.demoe.Service.S3Service;
import com.example.demoe.Service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.json.Path2;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GetCart {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private CartItemRepo cartItemRepo;
    @Autowired
    private DiscountRepo discountRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private ObjectMapper objectMapper;

    UnifiedJedis jedis = JedisSingleton.getInstance();
    public ResponseEntity<CartDtoMessage> getCart() throws IOException {
        Optional<User> authenticatedUser = userService.getAuthenticatedUser();
        if (!authenticatedUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new CartDtoMessage("User not authenticated", null));
        }

        User user = authenticatedUser.get();
        Long userId = user.getId();
        String redisKey = "cart:" + user.getId();


        Optional<Cart> cartOptional = cartRepo.findCart(userId);
        if (!cartOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new CartDtoMessage("Cart not found", null));
        }
        Cart cart = cartOptional.get();
        updateDiscountsInCart(cart,redisKey);


        if (jedis.exists(redisKey)) {
            CartDto cartDto = getCartFromRedis(redisKey);
            return ResponseEntity.ok(new CartDtoMessage("success", cartDto));
        }

        CartDto cartDto = createCartDto(cart);
        saveCartToRedis(redisKey, cartDto);

        return ResponseEntity.ok(new CartDtoMessage("success", cartDto));
    }

    private CartDto getCartFromRedis(String redisKey) throws IOException {
        Object jsonFromRedis = jedis.jsonGet(redisKey, new Path2("$"));
        String jsonArrayString = jsonFromRedis.toString();
        JSONArray jsonArray = new JSONArray(jsonArrayString);
        String jsonString = jsonArray.get(0).toString();

        return objectMapper.readValue(jsonString, CartDto.class);
    }

    //cái này nó còn trường hợp update discount mới nữa , cái này mới loaij bỏ discount hết hạn thôi ,
    private void updateDiscountsInCart(Cart cart,String redisKey) {
        for (CartItem cartItem : cart.getCartItems()) {
            if (cartItem.getDiscount() != null && cartItem.getDiscount().getEndDate() != null &&
                    cartItem.getDiscount().getEndDate().isBefore(LocalDateTime.now())) {
                cartItem.setDiscount(null);
                cartItemRepo.save(cartItem);

                if (jedis.exists(redisKey)) {
                    String pathToDiscount = "$.cartItems[?(@.id==" + cartItem.getId() + ")].discount";
                    jedis.jsonSet("cart:" + cart.getUser().getId(), new Path2(pathToDiscount), Optional.ofNullable(null));
                }

            }
            List<Discount> discountList=discountRepo.findDiscounts1(LocalDateTime.now(),cartItem.getProductId());

            Optional<Discount> discountlv2 = discountList.stream()
                    .filter(d -> d.getLevel() != null && d.getLevel() == 2&&d.getIsActive()==true)
                    .findFirst();
            Optional<Discount> discountlv1 = discountList.stream()
                    .filter(d -> d.getLevel() != null && d.getLevel() == 1&&d.getIsActive()==true)
                    .findFirst();
            if(discountlv2.isPresent()){
                cartItem.addDiscount(discountlv2.get());
                String pathToDiscount = "$.cartItems[?(@.id==" + cartItem.getId() + ")].discount";
                DiscountDtoCartRedis discountDtoCartRedis = DiscountDtoCartRedis.builder()
                        .startDate(discountlv2.get().getStartDate())
                        .endDate(discountlv2.get().getEndDate())
                        .discountValue(discountlv2.get().getDiscountValue())
                        .build();
                jedis.jsonSet("cart:" + cart.getUser().getId(), new Path2(pathToDiscount), discountDtoCartRedis);
            }
            else{
                if(discountlv1.isPresent()){
                    cartItem.addDiscount(discountlv1.get());
                    String pathToDiscount = "$.cartItems[?(@.id==" + cartItem.getId() + ")].discount";
                    DiscountDtoCartRedis discountDtoCartRedis = DiscountDtoCartRedis.builder()
                            .startDate(discountlv2.get().getStartDate())
                            .endDate(discountlv2.get().getEndDate())
                            .discountValue(discountlv2.get().getDiscountValue())
                            .build();
                    jedis.jsonSet("cart:" + cart.getUser().getId(), new Path2(pathToDiscount), discountDtoCartRedis);
                }
                }
            }
        }


    private CartDto createCartDto(Cart cart) {
        List<CartItemDto> cartItemDtoList = cart.getCartItems().stream()
                .map(cartItem -> {
                    DiscountDtoCartRedis discountDtoCartRedis;

                    if (cartItem.getDiscount() != null) {
                        discountDtoCartRedis = DiscountDtoCartRedis.builder()
                                .startDate(cartItem.getDiscount().getStartDate())
                                .endDate(cartItem.getDiscount().getEndDate())
                                .discountValue(cartItem.getDiscount().getDiscountValue())
                                .build();
                    } else {
                        // Gán giá trị mặc định nếu discount là null
                        discountDtoCartRedis = DiscountDtoCartRedis.builder()
                                .startDate(null)  // Hoặc một giá trị mặc định khác nếu cần
                                .endDate(null)    // Hoặc một giá trị mặc định khác nếu cần
                                .discountValue(BigDecimal.ZERO)  // Giá trị mặc định
                                .build();
                    }
                    CartItemDto cartItemDto = CartItemDto.builder()
                            .id(cartItem.getId())
                            .productId(cartItem.getProductId())
                            .quantity(cartItem.getQuantity())
                            .discount(discountDtoCartRedis)
                            .provarId(cartItem.getProVar().getId())
                            .productName(cartItem.getProductName())
                            .price(cartItem.getProVar().getPrice())
                            .max1Buy(cartItem.getMax1Buy())
                            .build();

                    String image = s3Service.getPresignedUrl(cartItem.getProVar().getImage());
                    cartItemDto.setImage(image);

                    List<Vars> varList = cartItem.getProVar().getVars().stream()
                            .map(var -> Vars.builder().key1(var.getKey1()).value(var.getValue()).build())
                            .collect(Collectors.toList());
                    cartItemDto.setVarList(varList);

                    return cartItemDto;
                })
                .collect(Collectors.toList());

        return new CartDto(cart.getId(), cartItemDtoList);
    }

    private void saveCartToRedis(String redisKey, CartDto cartDto) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(cartDto);
        jedis.jsonSet(redisKey, new Path2("$"), json);
    }
}

