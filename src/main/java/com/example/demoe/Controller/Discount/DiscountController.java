package com.example.demoe.Controller.Discount;

import com.example.demoe.Controller.Discount.Request.CreatepPerDiscountRequest;
import com.example.demoe.Controller.Discount.Request.UpdateDiscountPerProRequest;
import com.example.demoe.Dto.Discount.DiscountDtoMessage;
import com.example.demoe.Dto.Discount.ListDiscountDtoMessage;
import com.example.demoe.Dto.Product.ProductDto;
import com.example.demoe.Entity.Admin;
import com.example.demoe.Entity.product.Discount;
import com.example.demoe.Entity.product.Product;
import com.example.demoe.Helper.JedisSingleton;
import com.example.demoe.Repository.AdminRepo;
import com.example.demoe.Repository.DiscountRepo;
import com.example.demoe.Repository.ProductRepo;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.UnifiedJedis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/discount")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class DiscountController {
    @Autowired
    private DiscountRepo discountRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private AdminRepo adminRepo;
    //cái này là discount cho 1 loạt,tạo xong có áp dụng luôn rồi áp dụng luôn cho toàn bộ
    @PostMapping("/create")
    public ResponseEntity<DiscountDtoMessage> createDiscount(@RequestBody Discount discount) {
        System.out.println("discount"+discount.getDiscountValue());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Admin admin = (Admin) authentication.getPrincipal();
        String email = admin.getEmail();
        Optional<Admin> admin1 = adminRepo.findByEmail(email);

        if (!admin1.isPresent()) {
            return ResponseEntity.ok(new DiscountDtoMessage("not found admin"));
        }
        Hibernate.initialize(admin1.get().getDiscountList());

        //Nếu cái discount đang active của product >=1 thì không được add thêm nữa
        List<Discount> discount1=discountRepo.findDiscounts(discount.getStartDate(),discount.getEndDate(),true);
        if(discount1.size()>=1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DiscountDtoMessage("failed due to duplicate date"));
        }
        //discount này mặc định gửi lên là nó đã có level 2 rồi , xong mỗi cái product add discount cho cái discount lv2 này là active
        //còn cái discount lv1 thì setIsActive là false
        else{
            UnifiedJedis jedis = JedisSingleton.getInstance();
//            discount.setLevel(2);
            discountRepo.save(discount);
            admin1.get().addDiscount(discount);
            adminRepo.save(admin1.get());
            List<Product> productList=productRepo.findAll();
            for(Product product:productList){
                product.addDiscount(discount);
                productRepo.save(product);
            }
            List<Discount> discountList=discountRepo.findAllByLevel(1);
            for(Discount discount2:discountList){
                discount2.setIsActive(false);
                discountRepo.save(discount2);
            }
        }

        return ResponseEntity.ok(new DiscountDtoMessage("success",discount));
    }
    //add vào product

//    @PostMapping
    //cái này là discoun cho tưng product riêng lẻ
    //check thời gian tạo nữa không nó bị trùng

    //Cái này chỉ là create Discount cho 1 product thôi
    @PostMapping("/create1")
        public ResponseEntity<DiscountDtoMessage> createDiscount(@RequestBody CreatepPerDiscountRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Admin admin = (Admin) authentication.getPrincipal();
        String email = admin.getEmail();
        Optional<Admin> admin1 = adminRepo.findByEmail(email);

        if (!admin1.isPresent()) {
//            return ResponseEntity.ok(new ProductDto("not found admin"));
        }
        Optional<Product> product=productRepo.findById(request.getId());
        if(product.isPresent()){

        }
        Discount discount=request.getDiscount();
        List<Discount> discount2=discountRepo.findDiscountsByProductAndDateRangeAndIsActive(discount.getStartDate(),discount.getEndDate(),discount.getIsActive(),product.get().getId());
        for(Discount discount1:discount2){
           System.out.println("id"+discount1.getId());
        }
        boolean hasLevelOneDiscount = discount2.stream()
                .anyMatch(d -> d.getLevel() != null && d.getLevel() == 1);
        System.out.println("hasLevelOneDiscount"+hasLevelOneDiscount);
        if(hasLevelOneDiscount){
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DiscountDTO(discount2.get(), "Discount already exists"));
           return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DiscountDtoMessage("failed due to duplicate date"));
        }else {
//            discount.setLevel(1);
            discountRepo.save(discount);
            admin1.get().addDiscount(discount);
            product.get().addDiscount(discount);
            adminRepo.save(admin1.get());
            productRepo.save(product.get());
            return ResponseEntity.ok(new DiscountDtoMessage("success",discount));
        }

    }

    @CrossOrigin(origins = "http://localhost:5174")
    @GetMapping("/getAllDiscountLevel2")
    public ResponseEntity<List<Discount>> getAllDiscountLevel2() {
        System.out.println("hello1");
        System.out.println("hello1");
        List<Discount> discounts = discountRepo.findAllByLevel(2);
        for(Discount discount:discounts){
            if(discount.getStartDate().isBefore(LocalDateTime.now())||discount.getEndDate().isAfter(LocalDateTime.now())){
                discount.setIsActive(false);
                discountRepo.save(discount);
            }
        }

        return ResponseEntity.ok(discounts);
    }

    @CrossOrigin(origins = "http://localhost:5174")
    @DeleteMapping("/deleteDiscountLevel2/{id}")
    public ResponseEntity<DiscountDtoMessage> deleteDiscountLevel2(@PathVariable("id") Long id) {
        if (discountRepo.existsById(id)) {
            List<Product> productList=productRepo.findProductByDiscountId(id);
            for(Product pro:productList){
                pro.getDiscounts().removeIf(item -> item.getId().equals(id));
                productRepo.save(pro);
            }
            // Xóa bản ghi
            discountRepo.deleteById(id);
            // Trả về phản hồi thành công
            return ResponseEntity.ok(new DiscountDtoMessage("Discount deleted successfully.",id));
        } else {
            // Nếu không tìm thấy bản ghi, trả về lỗi 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DiscountDtoMessage("Discount not found."));
        }
    }
    @CrossOrigin(origins = "http://localhost:5174")
    @GetMapping("/getAllDiscountFor1Product/{id}")
    public ResponseEntity<ListDiscountDtoMessage> getAllDiscountFor1Product(@PathVariable("id") Long id) {
        System.out.println("id"+id);
        Optional<Product> product=productRepo.findById(id);
        if(product.isPresent()){
            List<Discount> discounts = discountRepo.findAllBy1Product(id);
            for(Discount discount:discounts){

                System.out.println("discountId"+discount.getId());
            }
            return ResponseEntity.ok(new ListDiscountDtoMessage(discounts,"success"));

        }
        else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ListDiscountDtoMessage("Product not found."));
        }

    }
    @CrossOrigin(origins = "http://localhost:5174")
    @DeleteMapping("/deleteAllDiscountFor1Product/{id}")
    public ResponseEntity<DiscountDtoMessage> deleteDiscountFor1Product(@PathVariable("id") Long id,@RequestParam("level") Long level,@RequestParam("productId") Long productId) {
        System.out.println("id"+id);

        if(level==1){
            Optional<Discount> discount=discountRepo.findById(id);
            List<Product> productList=discount.get().getProductList();
            for(Product pro:productList){
                pro.getDiscounts().removeIf(item -> item.getId().equals(id));
                productRepo.save(pro);
            }
            discountRepo.deleteById(id);

            return ResponseEntity.ok(new DiscountDtoMessage("success",id));

        }
        else if (level==2){
            Optional<Discount> discount=discountRepo.findById(id);
            if(discount.isPresent()){
                //chỉ xóa cái id trong  discounts trong   product id thôi chứ không phải cái list product
                Product product=productRepo.findById(productId).get();
                product.getDiscounts().removeIf(item -> item.getId().equals(id));
                productRepo.save(product);
                // loại bỏ tham chiếu của product trong discount
                discount.get().getProductList().removeIf(item -> item.getId().equals(productId));
                discountRepo.save(discount.get());
                // Trả về phản hồi này của bản ghi
                return ResponseEntity.ok(new DiscountDtoMessage("Discount deleted successfully.",id));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new DiscountDtoMessage("discount not found."));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new DiscountDtoMessage("Discount not found."));

    }

    @CrossOrigin(origins = "http://localhost:5174")
    @PostMapping("/updateAllDiscountFor1Product")
    public ResponseEntity<DiscountDtoMessage> updateDiscountFor1Product(@RequestBody UpdateDiscountPerProRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Admin admin = (Admin) authentication.getPrincipal();
        String email = admin.getEmail();
        Optional<Admin> admin1 = adminRepo.findByEmail(email);

        if (!admin1.isPresent()) {
//            return ResponseEntity.ok(new ProductDto("not found admin"));
        }
        Discount discount=request.getDiscount();
        System.out.println("value"+discount.getDiscountValue());
        Product product=productRepo.findById(request.getProductId()).get();
        if(!discount.getIsActive()){
            Discount discount3=discountRepo.findById(discount.getId()).get();
            discount3.setDiscountValue(discount.getDiscountValue());
            discount3.setEndDate(discount.getEndDate());
            discount3.setStartDate(discount.getStartDate());
            discount3.setType(discount.getType());
            discount3.setIsActive(discount.getIsActive());
            discountRepo.save(discount3);
            return ResponseEntity.ok(new DiscountDtoMessage("success",discount3,discount.getId()));

        }
        if(discount.getLevel()==1){
            List<Discount> discount2=discountRepo.findDiscountsByProductAndDateRangeAndIsActive(discount.getStartDate(),discount.getEndDate(),discount.getIsActive(),product.getId());
            long countLevelOneDiscounts = discount2.stream()
                    .filter(d -> d.getLevel() != null && d.getLevel() == 1)
                    .count();

            if(countLevelOneDiscounts==1){
               Discount discount3=discountRepo.findById(discount.getId()).get();
               discount3.setDiscountValue(discount.getDiscountValue());
               discount3.setEndDate(discount.getEndDate());
               discount3.setStartDate(discount.getStartDate());
               discount3.setType(discount.getType());
               discount3.setIsActive(discount.getIsActive());
               discountRepo.save(discount3);
            return ResponseEntity.ok(new DiscountDtoMessage("success",discount3,discount.getId()));

            }else {
                  ResponseEntity.status(HttpStatus.NOT_FOUND)
                       .body(new DiscountDtoMessage("failed due to duplicate date"));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new DiscountDtoMessage("Discount not found."));

    }
}
