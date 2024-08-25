//package com.example.demoe.Service;
//
//import com.example.demoe.Entity.product.Product;
//import com.example.demoe.Repository.ProductRepo;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisCallback;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//public class ProductService {
//    @Autowired
//    private ProductRepo productRepository;
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    private static final String PRODUCT_KEY_PREFIX = "product:";
//    private static final String PRODUCTS_SET_KEY = "products_set";
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    public String toJson(Product product) throws JsonProcessingException {
//        return objectMapper.writeValueAsString(product);
//    }
//    public void loadProductsToRedisWithPipeline() {
//        List<Product> products = productRepository.findAll();
//        String productsSetKey = "products_set";
//
//        List<Object> results = redisTemplate.executePipelined((RedisCallback<?>)connection -> {
//            for (Product product : products) {
//                // Chuyển đối tượng thành JSON
//                String productJson;
//                try {
//                    productJson = toJson(product);
//                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                    continue; // Bỏ qua sản phẩm này nếu không chuyển đổi được
//                }
//
//                byte[] productJsonBytes = productJson.getBytes();
//                connection.sAdd(productsSetKey.getBytes(), productJsonBytes);
//            }
//            return null;
//        });
//
//
//        // Kiểm tra kết quả và xử lý lỗi nếu cần thiết
//        for (int i = 0; i < results.size(); i++) {
//            Object result = results.get(i);
//            if (result instanceof Boolean && !(Boolean) result) {
//                // Xử lý lỗi, ví dụ: ghi lại log hoặc gửi lại lệnh
//                Product failedProduct = products.get(i / 2); // Mỗi sản phẩm có 2 lệnh: name và price
//                System.out.println("Failed to save product: " + failedProduct.getId());
//                // Gửi lại lệnh hoặc thực hiện các bước xử lý khác nếu cần
//            }
//        }
//    }
//    public List<Product> getProductsFromRedis() {
//        Set<Object> productJsonSet = redisTemplate.opsForSet().members(PRODUCTS_SET_KEY);
//
//        if (productJsonSet == null) {
//            return List.of();
//        }
//        return productJsonSet.stream()
//                .map(obj -> (String) obj)
//                .map(json -> {
//                    try {
//                        return objectMapper.readValue(json, Product.class);
//                    } catch (JsonProcessingException e) {
//                        // Xử lý lỗi chuyển đổi JSON
//                        e.printStackTrace();
//                        return null;
//                    }
//                })
//                .filter(product -> product != null) // Lọc các đối tượng null
//                .collect(Collectors.toList());
//    }
//}
