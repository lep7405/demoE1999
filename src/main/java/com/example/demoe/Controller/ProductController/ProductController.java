package com.example.demoe.Controller.ProductController;

import com.example.demoe.Controller.ProductController.CreateProductRequest.ExtraProductVariant;
import com.example.demoe.Controller.ProductController.CreateProductRequest.ExtraProductVariantList;
import com.example.demoe.Controller.ProductController.UpdateProductRequest.ExtraUpdate;
import com.example.demoe.Controller.ProductController.UpdateProductRequest.ExtraUpdateList;
import com.example.demoe.Controller.ProductController.UpdateProductRequest.ExtraValueUpdate;
import com.example.demoe.Dto.CommentDto;
import com.example.demoe.Dto.OrderPaid.OrderPaidDto;
import com.example.demoe.Dto.OrderPaid.OrderPaidItemDto;
import com.example.demoe.Dto.Product.ProDto;
import com.example.demoe.Dto.Product.ProductDetailDto;
import com.example.demoe.Dto.Product.ProductDto;
import com.example.demoe.Dto.Product.ProductRanDom.ListProDto;
import com.example.demoe.Dto.Product.ProductRanDom.ProDto1;
import com.example.demoe.Entity.Admin;
import com.example.demoe.Entity.Order.OrderPaid;
import com.example.demoe.Entity.User;
import com.example.demoe.Entity.product.*;
import com.example.demoe.Repository.*;
import com.example.demoe.Service.AdminService;
import com.example.demoe.Service.ElasticsearchService;
import com.example.demoe.Service.S3Service;
import com.example.demoe.Service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/product")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class ProductController {
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductVarRepo productVarRepo;
    @Autowired
    private VarRepo varRepo;
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private AdminRepo adminRepo;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private UserService userService;
    @Autowired
    private DiscountRepo discountRepo;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private CountProductRepo countProductRepo;
    @Autowired
    private CountPerMonthRepo countPerMonthRepo;
    @Autowired
    private ReviewRepo reviewRepo;
    @Autowired
    private OrderPaidRepo orderPaidRepo;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AdminService adminService;


    @PostMapping("/createProduct")
    public ResponseEntity<ProductDto> uploadProduct(
            @ModelAttribute ExtraProductVariantList extraProductVariantList
    ) throws IOException {
        Optional<Admin> admin1 =adminService.getAuthenticatedAdmin();
        if (!admin1.isPresent()) {
            return ResponseEntity.ok(new ProductDto("not found admin"));
        }
        System.out.println("productName" + extraProductVariantList.getProductName());
        System.out.println("productdes" + extraProductVariantList.getDescription());
        System.out.println("max1Buy" + extraProductVariantList.getMax1Buy());
        MultipartFile[] multipartFiles = extraProductVariantList.getFiles();

        if (multipartFiles != null) {
            System.out.println("Number of files: " + multipartFiles.length);
            for (MultipartFile file : multipartFiles) {
                System.out.println("File name: " + file.getOriginalFilename() + ", size: " + file.getSize());
            }
        } else {
            System.out.println("No files received in the request.");
        }


        for (ExtraProductVariant productVariantDTO : extraProductVariantList.getProductVariants()) {
            System.out.println("Price: " + productVariantDTO.getPrice());
            System.out.println("Stock: " + productVariantDTO.getStock());
            MultipartFile file = productVariantDTO.getFile();
            System.out.println("file: " + file.getOriginalFilename());
            if (productVariantDTO.getExtraValue() != null) {
                productVariantDTO.getExtraValue().forEach(extraValue -> {
                    System.out.println("Extra Value - Key: " + extraValue.getKey1() + ", Value: " + extraValue.getValue());
                });
            }
        }
        ;
        Optional<Product> product1 = productRepo.findByProductName(extraProductVariantList.getProductName());
        if (product1.isPresent()) {
            return ResponseEntity.ok(new ProductDto("product already exist"));
        }
        Optional<Category> category = categoryRepo.findById(Long.valueOf(extraProductVariantList.getCategoryId()));
        Product product = new Product();
        product.setProductName(extraProductVariantList.getProductName());
        product.setDescription(extraProductVariantList.getDescription());
        product.setMax1Buy(extraProductVariantList.getMax1Buy());
        product.setActive(extraProductVariantList.getActive());
        product.addCategory(category.get());
        product.setImages(s3Service.addtoS3(multipartFiles, "productImage"));
        product.setAdmin(admin1.get());

        productRepo.save(product);
        admin1.get().addProduct(product);

        for (ExtraProductVariant productVariantDTO : extraProductVariantList.getProductVariants()) {
            MultipartFile file = productVariantDTO.getFile();
            ProVar proVar = new ProVar();
            proVar.setPrice(new BigDecimal(productVariantDTO.getPrice()));
            proVar.setStock(Integer.valueOf((productVariantDTO.getStock())));
            proVar.setImage(s3Service.uploadToS3(file, "productVarImage"));
            productVarRepo.save(proVar);
            product.addProVar(proVar);
            if (productVariantDTO.getExtraValue() != null) {
                productVariantDTO.getExtraValue().forEach(extraValue -> {
                    System.out.println("Extra Value - Key: " + extraValue.getKey1() + ", Value: " + extraValue.getValue());
                    Var var = new Var();
                    var.setKey1(extraValue.getKey1());
                    var.setValue(extraValue.getValue());
                    varRepo.save(var);
                    proVar.addVar(var);
                    productVarRepo.save(proVar);
                });
            }
        }
        ;

        return ResponseEntity.ok(new ProductDto(product, "success"));
    }
    @PostMapping("/updateProduct")
    public ResponseEntity<String> updateProduct(
            @ModelAttribute ExtraUpdateList extraProductVariantList
    ) throws IOException {
        Product product=productRepo.findById(extraProductVariantList.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with email: " + extraProductVariantList.getId()));
        Product product1=productRepo.findByProductName(extraProductVariantList.getProductName())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with productName: " + extraProductVariantList.getProductName()));
        Optional<Category> category = categoryRepo.findById(Long.valueOf(extraProductVariantList.getCategoryId()));

        product.setProductName(extraProductVariantList.getProductName());
        product.setDescription(extraProductVariantList.getDescription());
        product.setMax1Buy(extraProductVariantList.getMax1Buy());
        product.setActive(extraProductVariantList.getActive());
        product.setCategories(new ArrayList<>());
        product.addCategory(category.get());

        MultipartFile[] multipartFiles = extraProductVariantList.getFiles();
        List<String > img=new ArrayList<>(); //cái list filename mới
        if (multipartFiles != null) {
            img=s3Service.addtoS3(multipartFiles,"productImage");
            for(String s:img){
                System.out.println(s);
            }
        } else {
            System.out.println("No files received in the request.");
        }
        System.out.println("productName" + extraProductVariantList.getProductName());
        System.out.println("productdes" + extraProductVariantList.getDescription());
        System.out.println("max1Buy" + extraProductVariantList.getMax1Buy());
        System.out.println("id" + extraProductVariantList.getId());


        List<String> imgs=product.getImages();
        for (String image : imgs) {

            System.out.println("Imgs name: " + image);
        }
        //iamge cua cai old image up len
        List<String> images = extraProductVariantList.getImages();
        List<String> filenames=new ArrayList<>();
        if (images != null) {
            System.out.println("Number of images: " + images.size());
            for (String image : images) {
                String filename=extractFilenameFromUrl(image);
                filenames.add(filename);
                System.out.println("Image name: " + filename);
            }
        } else {
            System.out.println("No images received in the request.");
        }


        List<String> common = new ArrayList<>(imgs);
        common.retainAll(filenames);

        for(String s:common){
            System.out.println("Common: " + s);
        }

        // Tìm phần có trong imgs nhưng không có trong filenames
        List<String> difference = new ArrayList<>(imgs);
        difference.removeAll(filenames);

        for(String dif:difference){
            System.out.println("Difference: " + dif);
            s3Service.dele(dif);
        }

        List<String> mergedList = new ArrayList<>(img);
        mergedList.addAll(common);
        product.setImages(mergedList);
        productRepo.save(product);



        List<ProVar> proVarList=product.getProVarList();
        List<Long> validIds = proVarList.stream()
                .map(ProVar::getId)
                .collect(Collectors.toList());
        for(Long id:validIds){
            System.out.println("validId: "+id);
        }
        List<Long> extraProductVariantList1 = extraProductVariantList.getProductVariants().stream()
                .filter(dto -> validIds.contains(dto.getId()))
                .map(ExtraUpdate::getId)
                .collect(Collectors.toList());


        List<Long> result = validIds.stream()
                .filter(id -> !extraProductVariantList1.contains(id))
                .collect(Collectors.toList());
        for(Long id:result){
            System.out.println("result: "+id);
        }
        for(Long ex:result){
            ProVar proVar = productVarRepo.findById(ex).get();
            for(Var var:proVar.getVars()){
                varRepo.delete(var);
            }
            productVarRepo.delete(proVar);
        }
        for (ExtraUpdate productVariantDTO : extraProductVariantList.getProductVariants()) {
            if(productVariantDTO.getId()==null){
                ProVar proVar = new ProVar();

                proVar.setImage(s3Service.uploadToS3(productVariantDTO.getFile(), "productVarImage"));
                proVar.setPrice(new BigDecimal(productVariantDTO.getPrice()));
                proVar.setStock(Integer.valueOf((productVariantDTO.getStock())));
                proVar.setProduct(product);
                productVarRepo.save(proVar);
                for(ExtraValueUpdate v:productVariantDTO.getExtraValue()){
                    Var var=new Var();
                    var.setKey1(v.getKey1());
                    var.setValue(v.getValue());
                    varRepo.save(var);
                    proVar.getVars().add(var);
                }
                productVarRepo.save(proVar);
                product.getProVarList().add(proVar);
                productRepo.save(product);
            }
            ProVar proVar = productVarRepo.findById(productVariantDTO.getId()).get();
            System.out.println("Id: " + productVariantDTO.getId());
            System.out.println("Price: " + productVariantDTO.getPrice());
            System.out.println("Stock: " + productVariantDTO.getStock());

            if (productVariantDTO.getFile() != null) {

                proVar.setImage(s3Service.uploadToS3(productVariantDTO.getFile(), "productVarImage"));
                System.out.println("file: " + productVariantDTO.getFile().getOriginalFilename());
            }
            else if(productVariantDTO.getImage()!=null){
                proVar.setImage(productVariantDTO.getImage());
                System.out.println("image: " + productVariantDTO.getImage());

            }
            proVar.setPrice(new BigDecimal(productVariantDTO.getPrice()));
            proVar.setStock(Integer.valueOf((productVariantDTO.getStock())));

            if (productVariantDTO.getExtraValue() != null) {
                productVariantDTO.getExtraValue().forEach(extraValueUpdate -> {
                    if (extraValueUpdate.getId() != null) {
                        Var var = varRepo.findById(extraValueUpdate.getId()).orElse(null);

                        if (var != null) {
                            var.setValue(extraValueUpdate.getValue());
                            var.setKey1(extraValueUpdate.getKey1());
                            varRepo.save(var);
                        } else {
                            // Handle the case where the var is not found
                            System.out.println("Var not found with id: " + extraValueUpdate.getId());
                        }
                    }
                    else{

                        Var var=new Var();
                        var.setKey1(extraValueUpdate.getKey1());
                        var.setValue(extraValueUpdate.getValue());
                        varRepo.save(var);
                        proVar.addVar(var);
                    }


                    System.out.println("Extra Value - Key: " + extraValueUpdate.getKey1() + ", Value: " + extraValueUpdate.getValue());
                });
            }
        }
        ;

        return ResponseEntity.ok("success");
    }

    @GetMapping("/get5ProductSale")
    public ResponseEntity<ListProDto> get5ProductSale() {
        List<Product> products = productRepo.findDiscounts1((LocalDateTime.now()),PageRequest.of(0, 7));
        List<ProDto1> products1 = new ArrayList<>();
        for (Product product : products) {
            ProDto1 proDto=new ProDto1();

            proDto.setId(product.getId());
            proDto.setProductName(product.getProductName());
            proDto.setImage(s3Service.getPresignedUrl(product.getImages().get(0)));
            BigDecimal minPrice = product.getProVarList().stream()
                    .map(ProVar::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO); // Giá trị mặc định nếu danh sách rỗng

            proDto.setPrice(minPrice);

            proDto.setNumberOfStars(product.getRateCount());
            proDto.setAverageStars(product.getAverageRate());

            List<Discount> discountList=discountRepo.findDiscounts1((new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),product.getId());
            Optional<Discount> discountlv2 = discountList.stream()
                    .filter(d -> d.getLevel() != null && d.getLevel() == 2)
                    .findFirst();
            Optional<Discount> discountlv1 = discountList.stream()
                    .filter(d -> d.getLevel() != null && d.getLevel() == 1)
                    .findFirst();
            if(discountlv2.isPresent()){
                proDto.setDiscount(discountlv2.get());
            }
            else{
                if(discountlv1.isPresent()){
                    proDto.setDiscount(discountlv1.get());
                }
            }
            products1.add(proDto);
        }

        return ResponseEntity.ok(new ListProDto(products1,0,"success"));
    }
    @GetMapping("/getAllProductRandom")
    public ResponseEntity<ListProDto> getAllProductRandom(@RequestParam("currentPage") int currentPage) {
        {
            List<Product> allProducts = new ArrayList<>();

            for (int i = 0; i <= currentPage; i++) {
                Pageable pageable = PageRequest.of(i, 1);
                Page<Product> productPage = productRepo.findAll(pageable);
                allProducts.addAll(productPage.getContent());
            }

            List<ProDto1> products1 = new ArrayList<>();
            for (Product product : allProducts) {
                ProDto1 proDto=new ProDto1();

                proDto.setId(product.getId());
                proDto.setProductName(product.getProductName());
                proDto.setImage(s3Service.getPresignedUrl(product.getImages().get(0)));
                BigDecimal minPrice = product.getProVarList().stream()
                        .map(ProVar::getPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO); // Giá trị mặc định nếu danh sách rỗng

                proDto.setPrice(minPrice);

                proDto.setNumberOfStars(product.getRateCount());
                proDto.setAverageStars(product.getAverageRate());

                List<Discount> discountList=discountRepo.findDiscounts1((new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),product.getId());
                Optional<Discount> discountlv2 = discountList.stream()
                        .filter(d -> d.getLevel() != null && d.getLevel() == 2&&d.getIsActive()==true)
                        .findFirst();
                Optional<Discount> discountlv1 = discountList.stream()
                        .filter(d -> d.getLevel() != null && d.getLevel() == 1&&d.getIsActive()==true)
                        .findFirst();
                if(discountlv2.isPresent()){
                    proDto.setDiscount(discountlv2.get());
                }
                else{
                    if(discountlv1.isPresent()){
                        proDto.setDiscount(discountlv1.get());
                    }
                }
                products1.add(proDto);
            }
            long totalRecords = productRepo.count();

            // Tính số lượng trang tổng cộng
            int totalPages = (int) Math.ceil((double) totalRecords / 1);
            return ResponseEntity.ok(new ListProDto(products1,totalPages,"success"));
        }
    }

    @GetMapping("/suggestion")
    public ResponseEntity<List<Map<String,String>>> suggestion(@RequestParam("prefix") String prefix) throws IOException {
        String normalizedString = Normalizer.normalize(prefix, Normalizer.Form.NFD);
        String prefix_no_diacritics = normalizedString.replaceAll("\\p{M}", "");
        List<Map<String,String>> productList=elasticsearchService.getSuggestions(prefix_no_diacritics);
        return ResponseEntity.ok(productList);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/detail/{id}")
    public ResponseEntity<ProductDetailDto> testPro(@PathVariable("id") Long id) {
        Product pro = productRepo.findById(id).get();
        List<Discount> discounts=new ArrayList<>();
        ProductDetailDto productDetailDto=ProductDetailDto.builder()
                .id(pro.getId())
                .productName(pro.getProductName())
                .description(pro.getDescription())
                .active(pro.getActive())
                .max1Buy(pro.getMax1Buy())

                .categories(pro.getCategories())
                .proVarList(pro.getProVarList())
                .build();
        List<Discount> discountList=discountRepo.findDiscounts1((new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),productDetailDto.getId());
        Optional<Discount> discountlv2 = discountList.stream()
                .filter(d -> d.getLevel() != null && d.getLevel() == 2&&d.getIsActive()==true)
                .findFirst();
        Optional<Discount> discountlv1 = discountList.stream()
                .filter(d -> d.getLevel() != null && d.getLevel() == 1&&d.getIsActive()==true)
                .findFirst();
        if(discountlv2.isPresent()){
            discounts.add(discountlv2.get());
            productDetailDto.setDiscountList(discounts);
        }
        else{
            if(discountlv1.isPresent()){
                discounts.add(discountlv1.get());
                productDetailDto.setDiscountList(discounts);
            }
        }

        List<String> img=new ArrayList<>();
        for(String s:pro.getImages()){
            String s1= s3Service.getPresignedUrl(s);
            img.add(s1);
        }
        productDetailDto.setImages(img);
        for(ProVar proVar:pro.getProVarList()){
                String s1= s3Service.getPresignedUrl(proVar.getImage());
            proVar.setImage(s1);
        }

        List<Review> commentList=pro.getComments();
        List<CommentDto> commentDtoList=new ArrayList<>();
        for(Review comment:commentList){
            List<String> commentIamgeList=new ArrayList<>();
            for(String s :comment.getContextImage()){

                String s1= s3Service.getPresignedUrl(s);
                commentIamgeList.add(s1);
            }
            CommentDto commentDto=CommentDto.builder()
                            .commentId(comment.getId())
                                    .productId(comment.getProduct().getId())
                                            .content(comment.getContent())
                                                    .contextImage(commentIamgeList)
                    .commentTime(comment.getCommentTime())
                                                            .userEmail(maskEmail(comment.getUser().getEmail())).build();

            commentDtoList.add(commentDto);
        }
        productDetailDto.setComments(commentDtoList);
        return ResponseEntity.ok(productDetailDto);
    }

    @GetMapping("/getAllCategory")
    public ResponseEntity<List<Category>> getAllCategory() {
        List<Category> categories = categoryRepo.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/getAllProduct")
    public ResponseEntity<ProDto> getAllProduct(@RequestParam("currentPage") int currentPage, @RequestParam("pageSize") int pageSize) {
        {
            Pageable pageable = PageRequest.of(currentPage, pageSize);
            Page<Product> productPage = productRepo.findAll(pageable);

            List<Product> products = productRepo.findAll();//?
            Short totalPage = (short) ((short) products.size()/pageSize +1);
            if (products.size() == 0) {
                return ResponseEntity.ok((new ProDto("Total product : 0")));
            }
            List<ProductDetailDto> products1 = new ArrayList<>();
            for (Product product : productPage) {
                ProductDetailDto productDetailDto = new ProductDetailDto();
                productDetailDto.setId(product.getId());
                productDetailDto.setProductName(product.getProductName());
                productDetailDto.setActive(product.getActive());
                productDetailDto.setPrice(product.getProVarList().getFirst().getPrice());
                if (product.getDiscounts().size() > 0) {
                    List<Discount> discountList=discountRepo.findDiscounts1((new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(),product.getId());
                   productDetailDto.setDiscountList(discountList);
                }
                productDetailDto.setImage(s3Service.getPresignedUrl(product.getImages().get(0)));
                Integer total = 0;
                Short totalVariant = 0;
                for (ProVar proVar : product.getProVarList()) {
                    total += proVar.getStock().intValue();
                    for (Var var : proVar.getVars()) {
                        totalVariant++;
                    }
                }

                productDetailDto.setTotalStock(total);
                productDetailDto.setTotalVariant(totalVariant);

                products1.add(productDetailDto);
            }

            return ResponseEntity.ok(new ProDto("success", (short) 10, products1));
        }
    }



    public static String extractFilenameFromUrl(String url) {
        try {
            URL urlObj = new URL(url);
            String path = urlObj.getPath();
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8.toString());

            int productImageIndex = decodedPath.indexOf("productImage");
            if (productImageIndex != -1) {
                String productImagePath = decodedPath.substring(productImageIndex);
                if (productImagePath.contains("?")) {
                    productImagePath = productImagePath.split("\\?")[0];
                }

                return productImagePath;
            } else {
                String[] pathParts = decodedPath.split("/");
                String filenameWithExtension = pathParts[pathParts.length - 1];

                if (filenameWithExtension.contains("?")) {
                    filenameWithExtension = filenameWithExtension.split("\\?")[0];
                }

                return filenameWithExtension;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PostMapping("/getProduct/{id}")
    public Product getProduct(@PathVariable("id") Long id) {
        return productRepo.findById(id).get();
    }



    //Elastic search
    @GetMapping("/importAllProductToElastic")
    public ResponseEntity<List<?>> importAllProductToElastic() {
        List<Product> allProducts = productRepo.findAll();
        List<?> productList=elasticsearchService.indexProducts(allProducts);
        return ResponseEntity.ok(productList);
    }
    @GetMapping("/getProductSearch")
    public ResponseEntity<List<ProDto1>> getProductSearch(@RequestParam("prefix") String prefix) {
        String normalizedString = Normalizer.normalize(prefix, Normalizer.Form.NFD);

        String prefix_no_diacritics = normalizedString.replaceAll("\\p{M}", "");
        List<ProDto1> productList=elasticsearchService.getProductSearch(prefix_no_diacritics);
        return ResponseEntity.ok(productList);
    }

    @GetMapping("/getAllProductFromElastic")
    public ResponseEntity<List<Product>> getAllProductFromElastic() {

        List<Product> productList=elasticsearchService.fetchDataFromElasticsearch();
        return ResponseEntity.ok(productList);
    }

    @GetMapping("/getRecommentSearch")
    public ResponseEntity<List<Map<String,String>>> RecommentSearch(@RequestParam("texts") String texts) {
        return ResponseEntity.ok(elasticsearchService.recommentSearch(texts));
    }
    @PostMapping("/createComment")
    public ResponseEntity<Review> createComment(@ModelAttribute ReviewRequest commentRequest) throws IOException, ExecutionException, InterruptedException {

        long startTime = System.currentTimeMillis(); // Bắt đầu đo thời gian toàn bộ phương thức

        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        long userFetchTime = System.currentTimeMillis(); // Đo thời gian lấy User
        System.out.println("Fetch user time: " + (userFetchTime - startTime) + "ms");

        Product product = productRepo.findById(commentRequest.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        long productFetchTime = System.currentTimeMillis(); // Đo thời gian lấy Product
        System.out.println("Fetch product time: " + (productFetchTime - userFetchTime) + "ms");

        List<OrderPaid> orderPaidList = orderPaidRepo.findAllByUserId(user.getId());
        long orderPaidFetchTime = System.currentTimeMillis(); // Đo thời gian lấy OrderPaid
        System.out.println("Fetch orderPaid list time: " + (orderPaidFetchTime - productFetchTime) + "ms");

        Boolean checked = false;
        Integer count=0;
        for (OrderPaid orderPaid : orderPaidList) {
            String jsonString = objectMapper.writeValueAsString(orderPaid.getPropertiesArray());
            Object o1 = objectMapper.readValue(jsonString, Object.class);
            System.out.println(o1);
            OrderPaidDto orderPaidDto = objectMapper.convertValue(o1, OrderPaidDto.class);
            System.out.println(orderPaidDto);

            if (orderPaidDto.getStatus().equals("Paid")) {
                for (OrderPaidItemDto orderPaidItemDto : orderPaidDto.getOrderPaidItemDtos()) {
                    System.out.println(orderPaidItemDto);
                    if (commentRequest.getProductId().equals(orderPaidItemDto.getProductId())) {
                        checked = true;
                        count++;
                        break;
                    }
                }
            }
        }

        if (!checked) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
//        List<Review> comments=reviewRepo.getListCommentByProductId(commentRequest.getProductId(),user.getId());
//        if(comments.size()>=count){
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .header("Error", "Chỉ được bình luận tối đa " + count + " lần")
//                    .body(null);
//        }
//        List<String> newImage     = new ArrayList<>();
        long imageUploadStartTime = System.currentTimeMillis();
//        List<CompletableFuture<String>> futures = new ArrayList<>();
//        for (MultipartFile file : commentRequest.getFiles()) {
//            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//                try {
//                    return s3Service.uploadToS3(file, "comment");
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//            futures.add(future);
//        }
//        List<String> newImage = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        List<String> newImage = s3Service.addtoS3improve(commentRequest.getFiles(), "comment");

        long imageUploadEndTime = System.currentTimeMillis(); // Đo thời gian upload hình ảnh
        System.out.println("Image upload time: " + (imageUploadEndTime - imageUploadStartTime) + "ms");

        Review comment = Review.builder()
                .content(commentRequest.getContent())
                .contextImage(newImage)
                .commentTime(LocalDate.now())
                .rateNumber(commentRequest.getRateNumber())
                .build();
        long commentSaveStartTime = System.currentTimeMillis();
        reviewRepo.save(comment);
        long commentSaveEndTime = System.currentTimeMillis(); // Đo thời gian lưu comment
        System.out.println("Comment save time: " + (commentSaveEndTime - commentSaveStartTime) + "ms");

        user.addComment(comment);
        product.addComment(comment);
        long userProductSaveStartTime = System.currentTimeMillis();
        userRepo.save(user);
        productRepo.save(product);
        long userProductSaveEndTime = System.currentTimeMillis(); // Đo thời gian lưu User và Product
        System.out.println("User & Product save time: " + (userProductSaveEndTime - userProductSaveStartTime) + "ms");

        long endTime = System.currentTimeMillis(); // Kết thúc đo thời gian toàn bộ phương thức
        System.out.println("Total time: " + (endTime - startTime) + "ms");

        return ResponseEntity.ok(comment);
    }


    @GetMapping("/getComment")
    public ResponseEntity<List<Review>> getComment(@RequestParam("productId") Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return ResponseEntity.ok(product.getComments());
    }

    public String maskEmail(String email) {
        int visibleLength = 3;
        String mask = "*****";

        if (email == null || email.length() <= visibleLength) {
            return email;
        }
        String visiblePart = email.substring(0, visibleLength);
        int atIndex = email.indexOf("@");

        if (atIndex != -1) {

            String domainPart = email.substring(atIndex);
            return visiblePart + mask + domainPart;
        } else {

            return visiblePart + mask;
        }
    }
}