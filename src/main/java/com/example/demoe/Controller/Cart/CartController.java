package com.example.demoe.Controller.Cart;

import com.example.demoe.Dto.Cart.CartDto;
import com.example.demoe.Dto.Cart.CartDtoMessage;
import com.example.demoe.Dto.Cart.CartItemDto;
import com.example.demoe.Dto.Cart.Vars;
import com.example.demoe.Entity.User;
import com.example.demoe.Entity.cart.Cart;
import com.example.demoe.Entity.cart.CartItem;
import com.example.demoe.Entity.product.Discount;
import com.example.demoe.Entity.product.ProVar;
import com.example.demoe.Entity.product.Product;
import com.example.demoe.Entity.product.Var;
import com.example.demoe.Repository.*;
//import com.example.demoe.Service.RedisService;
import com.example.demoe.Service.S3Service;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.json.Path2;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/cart")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class CartController {

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private CartItemRepo cartItemRepo;
    @Autowired
    private ProductVarRepo productVarRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private DiscountRepo discountRepo;
    @Autowired
    private S3Service s3Service;
    private final ObjectMapper objectMapper;
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/getCart")
    public ResponseEntity<CartDtoMessage> getCart() throws IOException {
        UnifiedJedis jedis = new UnifiedJedis("redis://localhost:6380");
        System.out.println("hello1");
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        System.out.println("hello2");
        if (!user1.isPresent()) {
            return null;
        }

        Long userId = user1.get().getId();
        String redisKey = "cart:" + userId;


        // Lưu dữ liệu JSON vào Redis

        // Kiểm tra giỏ hàng trong Redis
        System.out.println("have stored redis");
        Boolean checkRedis = jedis.exists(redisKey);

        if (checkRedis) {
            Object ob1=jedis.jsonGet(redisKey,new Path2("$")) ;
            System.out.println("Value from Redis: " + ob1);

            String jsonArrayString = ob1.toString();

// Nếu chắc chắn rằng mảng JSON chỉ chứa một phần tử, hãy lấy phần tử đó.
            JSONArray jsonArray = new JSONArray(jsonArrayString);
            String jsonString = jsonArray.get(0).toString();

// Chuyển đổi chuỗi JSON sang đối tượng CartDto
            CartDto cartDto = objectMapper.readValue(jsonString, CartDto.class);
            return ResponseEntity.ok(new CartDtoMessage("success", cartDto));
            // Nếu có trong Redis, trả về dữ liệu từ Redis
//            return ResponseEntity.ok(new CartDtoMessage("success", (CartDto) jedis.jsonGet(redisKey,new Path2("$"))));
        }
        Optional<Cart> cart=cartRepo.findCart(user1.get().getId());
        List<CartItemDto> cartItemDtoList = new ArrayList<>();
        for(CartItem cartItem:cart.get().getCartItems()){
            CartItemDto cartItemDto = CartItemDto.builder()
                    .id(cartItem.getId())
                    .productId(cartItem.getProductId())
                    .quantity(cartItem.getQuantity())
                    .discountValue(cartItem.getDiscountValue())
                    .provarId(cartItem.getProVar().getId())
                    .productName(cartItem.getProductName())
                    .price(cartItem.getProVar().getPrice())
                    .max1Buy(cartItem.getMax1Buy()).build();
            String image=s3Service.getPresignedUrl(cartItem.getProVar().getImage());
            cartItemDto.setImage(image);

            List<Vars> varList = new ArrayList<>();
            for(Var var:cartItem.getProVar().getVars()){
                varList.add(Vars.builder().key1(var.getKey1()).value(var.getValue()).build());
            }
            cartItemDto.setVarList(varList);
            cartItemDtoList.add(cartItemDto);
        }

        CartDto cartDto=new CartDto(cart.get().getId(),cartItemDtoList);
        String json = objectMapper.writeValueAsString(cartDto);

        jedis.jsonSet(redisKey, new Path2("$"), json);
        System.out.println("just store in redis");
//        redisTemplate.opsForValue().set(redisKey, cartDto);
        return ResponseEntity.ok(new CartDtoMessage("success",cartDto));
    }
    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/addToCart")
    public ResponseEntity<Cart> addToCart(@RequestBody AddToCartRequest request) throws JsonProcessingException {
        System.out.println("hellllllllllllllllllllllllllllllloooooooooooooooooo2");
        UnifiedJedis jedis = new UnifiedJedis("redis://localhost:6380");
        Long id=request.getId();
        Long provarId=request.getProvarId();
        int quantity=request.getQuantity();
        Product product=productRepo.findById(id).get();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        Optional<Cart> cart=cartRepo.findCart(user1.get().getId());

        Long userId = user1.get().getId();
        String redisKey = "cart:" + userId;
        // Lưu dữ liệu JSON vào Redis
        // Kiểm tra giỏ hàng trong Redis
        System.out.println("hellllllllllllllllllllllllllllllloooooooooooooooooo1");
        Boolean checkRedis = jedis.exists(redisKey);
        for(CartItem cartItem:cart.get().getCartItems()){
            if(cartItem.getProVar()!=null&&cartItem.getProVar().getId()==provarId){
                Short oldQuantity=cartItem.getQuantity();
                cartItem.setQuantity((short) (cartItem.getQuantity()+quantity));
                cartItemRepo.save(cartItem);
                if(checkRedis){
                    System.out.println("have stored redis");
                    String pathToQuantity = "$.cartItems[?(@.id==" + cartItem.getId() + ")].quantity";
                    System.out.println("" + pathToQuantity);
                    jedis.jsonSet("cart:" + user1.get().getId(), new Path2(pathToQuantity), request.getQuantity()+oldQuantity);
                }

                return ResponseEntity.ok(cart.get());
            }
        }
        CartItem cartItem=CartItem.builder().productId(id).quantity((short) quantity).build();
        cartItem.addPro(productVarRepo.findById(provarId).get());

        List<Discount> discountList=discountRepo.findDiscounts1((new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),product.getId());
        Optional<Discount> discountlv2 = discountList.stream()
                .filter(d -> d.getLevel() != null && d.getLevel() == 2&&d.getIsActive()==true)
                .findFirst();
        Optional<Discount> discountlv1 = discountList.stream()
                .filter(d -> d.getLevel() != null && d.getLevel() == 1&&d.getIsActive()==true)
                .findFirst();
        if(discountlv2.isPresent()){
            cartItem.setDiscountValue(BigDecimal.valueOf(discountlv2.get().getDiscountValue()));
        }
        else{
            if(discountlv1.isPresent()){
                cartItem.setDiscountValue(BigDecimal.valueOf(discountlv1.get().getDiscountValue()));
            }
        }
        cartItem.setProductName(product.getProductName());
        cartItemRepo.save(cartItem);
        System.out.println("hellllllllllllllllllllllllllllllloooooooooooooooooo3");
        cart.get().addCartItem(cartItem);
        cartRepo.save(cart.get());
        List<Vars> vars = new ArrayList<>();
        for(Var var:cartItem.getProVar().getVars()){
            vars.add(Vars.builder().key1(var.getKey1()).value(var.getValue()).build());
        }
            CartItemDto cartItemDto= CartItemDto.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .discountValue(cartItem.getDiscountValue())
                .provarId(cartItem.getProVar().getId())
                .productName(cartItem.getProductName())
                .price(cartItem.getProVar().getPrice())
                .max1Buy(cartItem.getMax1Buy())
                    .varList(vars)
                    .build();
        String image=s3Service.getPresignedUrl(cartItem.getProVar().getImage());
        cartItemDto.setImage(image);
        List<CartItemDto> cartItemDtoList=new ArrayList<>();
        cartItemDtoList.add(cartItemDto);
//        CartDto cartDto=new CartDto(cart.get().getId(),cartItemDtoList);
            String json = objectMapper.writeValueAsString(cartItemDto);
        jedis.jsonArrAppend(redisKey, Path2.of("$.cartItems"), json);
//            jedis.jsonSet(redisKey, new Path2("$.cartItems"), json);
            System.out.println("just store in redisss");
        return ResponseEntity.ok(cart.get());
    }

    @GetMapping("/clear")
    public ResponseEntity<String> clear() {

        Cart cart=cartRepo.findById(202L).get();
        for(CartItem cartItem:cart.getCartItems()){
            cartItem.setCart(null);
            cartItem.setProductId(null);
            cartItemRepo.save(cartItem);
            ProVar proVar=cartItem.getProVar();
            proVar.setCartItem(null);
            productVarRepo.save(proVar);
        }
        cartRepo.save(cart);

        return ResponseEntity.ok("success");
    }

    @PostMapping("/updateCartItem")
    public ResponseEntity<CartItemDto> updateCartItem(@RequestBody UpdateCartItemRequest request){
        UnifiedJedis jedis = new UnifiedJedis("redis://localhost:6380");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();
            String email = user.getEmail();
            Optional<User> user1 = userRepo.findByEmail(email);
            Optional<Cart> cart = cartRepo.findCart(user1.get().getId());


            CartItem cartItem = cartItemRepo.findById(request.getId()).get();
            cartItem.setQuantity(request.getQuantity());
            cartItemRepo.save(cartItem);



            String pathToQuantity = "$.cartItems[?(@.id==" + request.getId() + ")].quantity";
            System.out.println("" + pathToQuantity);
            jedis.jsonSet("cart:" + user1.get().getId(), new Path2(pathToQuantity), request.getQuantity());
            return ResponseEntity.ok(CartItemDto.builder()
                    .id(cartItem.getId())
                    .quantity(cartItem.getQuantity())
                    .build());

    }

    @DeleteMapping("/deleteCartItem/{id}")
    public ResponseEntity<DeleteResponse> deleteCartItem(@PathVariable("id") Long id) {
        UnifiedJedis jedis = new UnifiedJedis("redis://localhost:6380");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        Optional<Cart> cart = cartRepo.findCart(user1.get().getId());
        ProVar proVar=cartItemRepo.findById(id).get().getProVar();
        proVar.setCartItem(null);
        productVarRepo.save(proVar);
        cartItemRepo.deleteById(id);
        String key="cart:"+user1.get().getId();
        jedis.jsonDel(key,new Path2("$.cartItems[?(@.id==" + id + ")]"));
        return ResponseEntity.ok(new DeleteResponse("success",id));
    }

    @PostMapping("/clearCart")
    public ResponseEntity<String> clearCart() {

        UnifiedJedis jedis = new UnifiedJedis("redis://localhost:6380");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        Optional<Cart> cart = cartRepo.findCart(user1.get().getId());
        for(CartItem cartItem:cart.get().getCartItems()){
            cartItem.setCart(null);
            cartItem.setProductId(null);
            cartItemRepo.save(cartItem);

            ProVar proVar=cartItem.getProVar();
            proVar.setCartItem(null);
            productVarRepo.save(proVar);
            cartItemRepo.delete(cartItem);
        }
        cart.get().setCartItems(new ArrayList<>());
        cartRepo.save(cart.get());
        String key  = "cart:"+user1.get().getId();
        String pathToQuantity = "$.cartItems";
        String newValue = "[]";
        jedis.jsonDel(key);
        return ResponseEntity.ok("success");
    }



    @GetMapping("/test")
    public ResponseEntity<String> test(@RequestParam("quantity") int quantity) {
        UnifiedJedis jedis = new UnifiedJedis("redis://localhost:6380");
//        String pathToQuantity = "$.cartItems[?(@.id==203)].max1Buy";
//
//// Giá trị quantity mới
//        int newQuantity = 20;
//
//// Cập nhật giá trị quantity trong Redis
//        jedis.jsonSet("cart:52", new Path2(pathToQuantity), newQuantity);

        String pathToQuantityy = "$.cartItems[?(@.id==" + quantity + ")].quantity";

// Giá trị quantity mới
        int newQuantityy = 7;

// Cập nhật giá trị quantity trong Redis
        jedis.jsonSet("cart:52", new Path2(pathToQuantityy), newQuantityy);
//
//
//        String pathToQuantity1 = "$.cartItems[?(@.id==202)].max1Buy";
//
//// Giá trị quantity mới
//        int newQuantity1 = 30;
//
//// Cập nhật giá trị quantity trong Redis
//        jedis.jsonSet("cart:52", new Path2(pathToQuantity1), newQuantity1);
        return ResponseEntity.ok("success");
    }
}
