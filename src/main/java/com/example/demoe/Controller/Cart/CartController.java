package com.example.demoe.Controller.Cart;

import com.example.demoe.Controller.Cart.CartHelper.AddToCartRequest;
import com.example.demoe.Controller.Cart.CartHelper.DeleteResponse;
import com.example.demoe.Controller.Cart.CartHelper.UpdateCartItemRequest;
import com.example.demoe.Dto.Cart.CartDto;
import com.example.demoe.Dto.Cart.CartDtoMessage;
import com.example.demoe.Dto.Cart.CartItemDto;
import com.example.demoe.Dto.Cart.Vars;
import com.example.demoe.Dto.Discount.DiscountDtoCartRedis;
import com.example.demoe.Entity.User;
import com.example.demoe.Entity.cart.Cart;
import com.example.demoe.Entity.cart.CartItem;
import com.example.demoe.Entity.product.Discount;
import com.example.demoe.Entity.product.ProVar;
import com.example.demoe.Entity.product.Product;
import com.example.demoe.Entity.product.Var;
import com.example.demoe.Helper.JedisSingleton;
import com.example.demoe.Repository.*;
//import com.example.demoe.Service.RedisService;
import com.example.demoe.Service.CartService.GetCart;
import com.example.demoe.Service.S3Service;
import com.example.demoe.Service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.json.Path2;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Autowired
    private UserService userService;
    private final ObjectMapper objectMapper;

    @Autowired
    private GetCart getCart;

    @GetMapping("/getCart")
    public ResponseEntity<CartDtoMessage> getCart() throws IOException {
        return getCart.getCart();
    }
//    @GetMapping("/getCart")
//    public ResponseEntity<CartDtoMessage> getCart() throws IOException {
//        UnifiedJedis jedis = JedisSingleton.getInstance();
//        System.out.println("hello1");
//        Optional<User> user1 = userService.getAuthenticatedUser();
//        System.out.println("hello2");
//        Long userId = user1.get().getId();
//        String redisKey = "cart:" + userId;
//
//        Optional<Cart> cart=cartRepo.findCart(user1.get().getId());
//       if(jedis.exists(redisKey)){
//           for (CartItem cartItem : cart.get().getCartItems()) {
//               if (cartItem.getDiscount() != null && cartItem.getDiscount().getEndDate() != null &&
//                       cartItem.getDiscount().getEndDate().isBefore(LocalDate.now())) {
//                   cartItem.setDiscount(null);
//                   cartItemRepo.save(cartItem);
//                   String pathToQuantity = "$.cartItems[?(@.id==" + cartItem.getId() + ")].discount";
//                   System.out.println("" + pathToQuantity);
//                   jedis.jsonSet("cart:" + user1.get().getId(), new Path2(pathToQuantity), Optional.ofNullable(null));
//               }
//           }
//       }
//        if (!user1.isPresent()) {
//            return null;
//        }
//        // Lưu dữ liệu JSON vào Redis
//
//        // Kiểm tra giỏ hàng trong Redis
//        System.out.println("have stored redis");
//        Boolean checkRedis = jedis.exists(redisKey);
//
//        if (checkRedis) {
//            Object ob1=jedis.jsonGet(redisKey,new Path2("$")) ;
//            System.out.println("Value from Redis: " + ob1);
//
//            String jsonArrayString = ob1.toString();
//
//// Nếu chắc chắn rằng mảng JSON chỉ chứa một phần tử, hãy lấy phần tử đó.
//            JSONArray jsonArray = new JSONArray(jsonArrayString);
//            String jsonString = jsonArray.get(0).toString();
//
//// Chuyển đổi chuỗi JSON sang đối tượng CartDto
//            CartDto cartDto = objectMapper.readValue(jsonString, CartDto.class);
//            return ResponseEntity.ok(new CartDtoMessage("success", cartDto));
//            // Nếu có trong Redis, trả về dữ liệu từ Redis
////            return ResponseEntity.ok(new CartDtoMessage("success", (CartDto) jedis.jsonGet(redisKey,new Path2("$"))));
//        }
////        Optional<Cart> cart=cartRepo.findCart(user1.get().getId());
//        List<CartItemDto> cartItemDtoList = new ArrayList<>();
//        for(CartItem cartItem:cart.get().getCartItems()){
//            CartItemDto cartItemDto = CartItemDto.builder()
//                    .id(cartItem.getId())
//                    .productId(cartItem.getProductId())
//                    .quantity(cartItem.getQuantity())
//                    .discountValue(Optional.ofNullable(cartItem.getDiscount())
//                            .map(discount -> BigDecimal.valueOf(discount.getDiscountValue()))
//                            .orElse(BigDecimal.ZERO))
//                    .provarId(cartItem.getProVar().getId())
//                    .productName(cartItem.getProductName())
//                    .price(cartItem.getProVar().getPrice())
//                    .max1Buy(cartItem.getMax1Buy()).build();
//            String image=s3Service.getPresignedUrl(cartItem.getProVar().getImage());
//            cartItemDto.setImage(image);
//
//            List<Vars> varList = new ArrayList<>();
//            for(Var var:cartItem.getProVar().getVars()){
//                varList.add(Vars.builder().key1(var.getKey1()).value(var.getValue()).build());
//            }
//            cartItemDto.setVarList(varList);
//            cartItemDtoList.add(cartItemDto);
//        }
//
//        CartDto cartDto=new CartDto(cart.get().getId(),cartItemDtoList);
//        String json = objectMapper.writeValueAsString(cartDto);
//
//        jedis.jsonSet(redisKey, new Path2("$"), json);
//        System.out.println("just store in redis");
////        redisTemplate.opsForValue().set(redisKey, cartDto);
//        return ResponseEntity.ok(new CartDtoMessage("success",cartDto));
//    }
    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/addToCart")
    public ResponseEntity<String> addToCart(@RequestBody AddToCartRequest request) throws JsonProcessingException {
        System.out.println("hellllllllllllllllllllllllllllllloooooooooooooooooo2");
        Long id=request.getId();
        Long provarId=request.getProvarId();
        int quantity=request.getQuantity();


        Product product=productRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        User user = userService.getAuthenticatedUser().orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart=cartRepo.findCart(user.getId()).orElseThrow(() -> new ResourceNotFoundException("Cart not found with userId: " + user.getId()));
        ProVar proVar=productVarRepo.findById(provarId).orElseThrow(() -> new ResourceNotFoundException("ProVar not found with id: " + provarId));

        //Check stock rồi mới làm tiếp
        int totalQuantity = quantity;
        for (CartItem cartItem : cart.getCartItems()) {
            if (cartItem.getProVar() != null && cartItem.getProVar().getId().equals(provarId)) {
                totalQuantity = cartItem.getQuantity() + quantity;
                break;
            }
        }
        if (totalQuantity > proVar.getStock()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Exceed stock");
        }


        // Lưu dữ liệu JSON vào Redis
        // Kiểm tra giỏ hàng trong Redis
        UnifiedJedis jedis = JedisSingleton.getInstance();
        String redisKey = "cart:" + user.getId();
        Boolean checkRedis = jedis.exists(redisKey);

        // Trường hợp có sẵn cartItem đó trong redis rồi
        for(CartItem cartItem:cart.getCartItems()){
            if(cartItem.getProVar()!=null&&cartItem.getProVar().getId()==provarId){
                Short oldQuantity=cartItem.getQuantity();
                cartItem.setQuantity((short) (cartItem.getQuantity()+quantity));
                cartItemRepo.save(cartItem);
                if(checkRedis){
                    System.out.println("have stored redis");
                    String pathToQuantity = "$.cartItems[?(@.id==" + cartItem.getId() + ")].quantity";
                    System.out.println("" + pathToQuantity);
                    jedis.jsonSet("cart:" + user.getId(), new Path2(pathToQuantity), request.getQuantity()+oldQuantity);


                    String pathToDiscount = "$.cartItems[?(@.id==" + cartItem.getId() + ")].discount";
                    List<Discount> discountList=discountRepo.findDiscounts1((new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),product.getId());
                    Optional<Discount> discountlv2 = discountList.stream()
                            .filter(d -> d.getLevel() != null && d.getLevel() == 2&&d.getIsActive()==true)
                            .findFirst();
                    if(discountlv2.isPresent()){
                        cartItem.addDiscount(discountlv2.get());
                        DiscountDtoCartRedis discountDtoCartRedis = DiscountDtoCartRedis.builder()
                                .startDate(cartItem.getDiscount().getStartDate())
                                .endDate(cartItem.getDiscount().getEndDate())
                                .discountValue(cartItem.getDiscount().getDiscountValue())
                                .build();
                        jedis.jsonSet("cart:" + user.getId(), new Path2(pathToDiscount), discountDtoCartRedis);
                    }
                    else{
                        Optional<Discount> discountlv1 = discountList.stream()
                                .filter(d -> d.getLevel() != null && d.getLevel() == 1&&d.getIsActive()==true)
                                .findFirst();
                        if(discountlv1.isPresent()){
                            cartItem.addDiscount(discountlv1.get());
                            DiscountDtoCartRedis discountDtoCartRedis = DiscountDtoCartRedis.builder()
                                    .startDate(cartItem.getDiscount().getStartDate())
                                    .endDate(cartItem.getDiscount().getEndDate())
                                    .discountValue(cartItem.getDiscount().getDiscountValue())
                                    .build();
                            jedis.jsonSet("cart:" + user.getId(), new Path2(pathToDiscount), discountDtoCartRedis);
                        }
                    }
                    return ResponseEntity.ok("Add to success");
                }

            }
        }


        //Trường hợp chưa có cartItem trong redis rồi
        //B1 Tìm cái Xây dụng cái cartItem và cho nó quan hệ với ProVar tại nếu như mà ProVar tăng giá thì nó cũng sẽ tăng theo
        CartItem cartItem=CartItem.builder().productId(id).quantity((short) quantity).max1Buy(product.getMax1Buy()).build();
        ProVar proVar1=productVarRepo.findById(provarId).get();
        proVar1.addCartItem(cartItem);
        productVarRepo.save(proVar1);



        //chỉnh lại discount và chỉ lưu cái discount active duy nhất thôi
        List<Discount> discountList=discountRepo.findDiscounts1((new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),product.getId());
        Optional<Discount> discountlv2 = discountList.stream()
                .filter(d -> d.getLevel() != null && d.getLevel() == 2&&d.getIsActive()==true)
                .findFirst();
        if(discountlv2.isPresent()){
            cartItem.addDiscount(discountlv2.get());
        }
        else{
            Optional<Discount> discountlv1 = discountList.stream()
                    .filter(d -> d.getLevel() != null && d.getLevel() == 1&&d.getIsActive()==true)
                    .findFirst();
            if(discountlv1.isPresent()){
                cartItem.addDiscount(discountlv1.get());
            }
        }
        cartItem.setProductName(product.getProductName());
        cartItemRepo.save(cartItem);

        // tạo cái cartItemDto cho nó bớt dữ liệu từ cartItem lại
        System.out.println("hellllllllllllllllllllllllllllllloooooooooooooooooo3");
        cart.addCartItem(cartItem);
        cartRepo.save(cart);
        List<Vars> vars = new ArrayList<>();
        for(Var var:cartItem.getProVar().getVars()){
            vars.add(Vars.builder().key1(var.getKey1()).value(var.getValue()).build());
        }
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
            CartItemDto cartItemDto= CartItemDto.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                    .discount(discountDtoCartRedis)
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
        if(!checkRedis){
            String json = objectMapper.writeValueAsString(cartItemDto);
            jedis.jsonArrAppend(redisKey, Path2.of("$.cartItems"), json);
//            jedis.jsonSet(redisKey, new Path2("$.cartItems"), json);
        }
        return ResponseEntity.ok("Add to success");
    }

    @GetMapping("/clear")
    public ResponseEntity<String> clear() {

        Cart cart=cartRepo.findById(202L).get();
        for(CartItem cartItem:cart.getCartItems()){
            cartItem.setCart(null);
            cartItem.setProductId(null);
            ProVar proVar=cartItem.getProVar();
            proVar.getCartItemList().remove(cartItem);
            productVarRepo.save(proVar);
            cartItem.setProVar(null);
            cartItemRepo.save(cartItem);
        }
        cartRepo.save(cart);

        return ResponseEntity.ok("success");
    }

    @PostMapping("/updateCartItem")
    public ResponseEntity<CartItemDto> updateCartItem(@RequestBody UpdateCartItemRequest request) {
        UnifiedJedis jedis = JedisSingleton.getInstance();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        Optional<Cart> cart = cartRepo.findCart(user1.get().getId());
        CartItem cartItem = cartItemRepo.findById(request.getId()).get();
        cartItem.setQuantity(request.getQuantity());
        cartItemRepo.save(cartItem);
        String pathToQuantity = "$.cartItems[?(@.id==" + request.getId() + ")].quantity";
        jedis.jsonSet("cart:" + user1.get().getId(), new Path2(pathToQuantity), request.getQuantity());
        ResponseEntity<CartItemDto> response = ResponseEntity.ok(CartItemDto.builder()
                .id(cartItem.getId())
                .quantity(cartItem.getQuantity())
                .build());
        return response;
    }


    @DeleteMapping("/deleteCartItem/{id}")
    public ResponseEntity<DeleteResponse> deleteCartItem(@PathVariable("id") Long id) {
        UnifiedJedis jedis = JedisSingleton.getInstance();
        User user = userService.getAuthenticatedUser().orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Optional<Cart> cart = cartRepo.findCart(user.getId());
        CartItem cartItem=cartItemRepo.findById(id).get();
        ProVar proVar=cartItem.getProVar();
        proVar.getCartItemList().remove(cartItem);
        productVarRepo.save(proVar);
        cartItemRepo.deleteById(id);
        String key="cart:"+user.getId();
        jedis.jsonDel(key,new Path2("$.cartItems[?(@.id==" + id + ")]"));
        return ResponseEntity.ok(new DeleteResponse("success",id));
    }

    @PostMapping("/clearCart")
    public ResponseEntity<String> clearCart() {

        UnifiedJedis jedis = JedisSingleton.getInstance();
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
            proVar.getCartItemList().remove(cartItem);
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

    @PostMapping("/checkQuantity")
    public ResponseEntity<String> checkQuantity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        System.out.println("hello2");
        Long userId = user1.get().getId();
        Optional<Cart> cart=cartRepo.findCart(user1.get().getId());
        for(CartItem cartItem:cart.get().getCartItems()) {
            if(cartItem.getQuantity()>cartItem.getProVar().getStock()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("exceed stock");
            }
        }
        return ResponseEntity.ok("success");
    }
}
