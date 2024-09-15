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
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        Admin admin = (Admin) authentication.getPrincipal();
//        String email = admin.getEmail();
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
//        if(product1.isPresent()){
//
//        }

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

        // Tìm phần chung giữa hai danh sách , cái list filename cũ
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
        // Tạo danh sách mới để chứa kết quả gộp
        List<String> mergedList = new ArrayList<>(img); // Sao chép các phần tử của img vào mergedList
        mergedList.addAll(common);
        product.setImages(mergedList);
        productRepo.save(product);


        //product có 1 tập hợp id của proVar , thiếu cái nào gửi lên , delete cái đó luôn
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
//        for(ExtraUpdate ex:extraProductVariantList1){
//            System.out.println("valid ex: "+ex.getId());
//        }
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


        // Tính số lượng trang tổng cộng

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


    //cái này thuộc phần elastic Sea
    @GetMapping("/suggestion")
    public ResponseEntity<List<Map<String,String>>> suggestion(@RequestParam("prefix") String prefix) throws IOException {
        String normalizedString = Normalizer.normalize(prefix, Normalizer.Form.NFD);

        // Loại bỏ các ký tự dấu (diacritics)
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

            // Tìm vị trí của "productImage"
            int productImageIndex = decodedPath.indexOf("productImage");
            if (productImageIndex != -1) {
                // Trả về phần từ "productImage" đến hết đường dẫn
                String productImagePath = decodedPath.substring(productImageIndex);

                // Loại bỏ tham số truy vấn nếu có
                if (productImagePath.contains("?")) {
                    productImagePath = productImagePath.split("\\?")[0];
                }

                return productImagePath;
            } else {
                // Nếu không tìm thấy "productImage", trả về toàn bộ tên tệp
                String[] pathParts = decodedPath.split("/");
                String filenameWithExtension = pathParts[pathParts.length - 1];

                // Loại bỏ tham số truy vấn nếu có
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

        // Loại bỏ các ký tự dấu (diacritics)
        String prefix_no_diacritics = normalizedString.replaceAll("\\p{M}", "");
        List<ProDto1> productList=elasticsearchService.getProductSearch(prefix_no_diacritics);
        return ResponseEntity.ok(productList);
    }

    @GetMapping("/getAllProductFromElastic")
    public ResponseEntity<List<Product>> getAllProductFromElastic() {

        List<Product> productList=elasticsearchService.fetchDataFromElasticsearch();
        return ResponseEntity.ok(productList);
    }
    @GetMapping("/prefixProduct")
    public ResponseEntity<List<Product>> prefixProduct(@RequestParam("prefix") String prefix) {

        List<Product> productList=elasticsearchService.prefixProduct(prefix);
        return ResponseEntity.ok(productList);
    }

    @GetMapping("/recommentSearch")
    public ResponseEntity<List<Map<String,String>>> recommentSearch(@RequestParam("prefix") String prefix) {

        List<Map<String,String>> productList=elasticsearchService.prefixProduct2(prefix);
        return ResponseEntity.ok(productList);
    }
    // kết thúc elastic




    //get 10 sản phẩm có số lượt count nhiều nhất



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
        int visibleLength = 3; // Số ký tự đầu được hiển thị
        String mask = "*****"; // Phần còn lại thay bằng dấu sao

        if (email == null || email.length() <= visibleLength) {
            return email; // Nếu email quá ngắn thì không mask
        }

        // Cắt lấy 3 ký tự đầu tiên
        String visiblePart = email.substring(0, visibleLength);

        // Tìm vị trí của dấu '@'
        int atIndex = email.indexOf("@");

        if (atIndex != -1) {
            // Giữ nguyên phần sau '@'
            String domainPart = email.substring(atIndex);
            return visiblePart + mask + domainPart;
        } else {
            // Trường hợp không tìm thấy '@', chỉ mask phần còn lại
            return visiblePart + mask;
        }
    }






    @GetMapping("/getProduct/{id}")
    public Product getProduct() {
        return productRepo.findById(1L).get();
    }
    @GetMapping("/testProvar/{id}")
    public ResponseEntity<?> testProvar(@PathVariable("id") Long id) {
        ProVar proVar = productVarRepo.findById(id).get();
        return ResponseEntity.ok(proVar);
    }

    @PostMapping("/updateProduct1")
    public ResponseEntity<String> updateProduct2(@ModelAttribute ExtraUpdateList extraProductVariantList) throws IOException {
        Optional<Product> productOptional = productRepo.findById(extraProductVariantList.getId());
        if (!productOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }

        Product product = productOptional.get();
        updateProductDetails(product, extraProductVariantList);

        List<String> newImages = handleUploadedFiles(extraProductVariantList.getFiles());
        List<String> existingImages = product.getImages();
        List<String> imagesToRetain = extractImageFilenames(extraProductVariantList.getImages());

        updateProductImages(product, newImages, existingImages, imagesToRetain);

        updateProductVariants(product, extraProductVariantList);

        productRepo.save(product);
        return ResponseEntity.ok("success");
    }

    private void updateProductDetails(Product product, ExtraUpdateList extraProductVariantList) {
        product.setProductName(extraProductVariantList.getProductName());
        product.setDescription(extraProductVariantList.getDescription());
        product.setMax1Buy(extraProductVariantList.getMax1Buy());
        product.setActive(extraProductVariantList.getActive());

        categoryRepo.findById(Long.valueOf(extraProductVariantList.getCategoryId()))
                .ifPresent(product::addCategory);
    }

    private List<String> handleUploadedFiles(MultipartFile[] files) throws IOException {
        if (files != null) {
            return s3Service.addtoS3(files, "productImage");
        }
        return new ArrayList<>();
    }

    private List<String> extractImageFilenames(List<String> imageUrls) {
        if (imageUrls != null) {
            return imageUrls.stream()
                    .map(this::extractFilenameFromUrl2)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private void updateProductImages(Product product, List<String> newImages, List<String> existingImages, List<String> imagesToRetain) {
        List<String> imagesToDelete = new ArrayList<>(existingImages);
        imagesToDelete.removeAll(imagesToRetain);

        imagesToDelete.forEach(s3Service::dele);

        List<String> mergedImages = new ArrayList<>(newImages);
        mergedImages.addAll(imagesToRetain);
        product.setImages(mergedImages);
    }

    private void updateProductVariants(Product product, ExtraUpdateList extraProductVariantList) throws IOException {
        List<Long> validIds = product.getProVarList().stream()
                .map(ProVar::getId)
                .collect(Collectors.toList());

        List<Long> extraVariantIds = extraProductVariantList.getProductVariants().stream()
                .filter(dto -> validIds.contains(dto.getId()))
                .map(ExtraUpdate::getId)
                .collect(Collectors.toList());

        List<Long> idsToDelete = validIds.stream()
                .filter(id -> !extraVariantIds.contains(id))
                .collect(Collectors.toList());

        deleteOldVariants(idsToDelete);
        saveOrUpdateVariants(product, extraProductVariantList);
    }

    private void deleteOldVariants(List<Long> idsToDelete) {
        for (Long id : idsToDelete) {
            productVarRepo.findById(id).ifPresent(proVar -> {
                proVar.getVars().forEach(varRepo::delete);
                productVarRepo.delete(proVar);
            });
        }
    }

    private void saveOrUpdateVariants(Product product, ExtraUpdateList extraProductVariantList) throws IOException {
        for (ExtraUpdate dto : extraProductVariantList.getProductVariants()) {
            ProVar proVar;
            if (dto.getId() == null) {
                proVar = createNewVariant(dto, product);
            } else {
                proVar = updateExistingVariant(dto);
            }
            updateVariantValues(proVar, dto.getExtraValue());
            productVarRepo.save(proVar);
            product.getProVarList().add(proVar);
        }
    }

    private ProVar createNewVariant(ExtraUpdate dto, Product product) throws IOException {
        ProVar proVar = new ProVar();
        proVar.setImage(s3Service.uploadToS3(dto.getFile(), "productVarImage"));
        proVar.setPrice(new BigDecimal(dto.getPrice()));
        proVar.setStock(Integer.valueOf((dto.getStock())));
        proVar.setProduct(product);
        return proVar;
    }

    private ProVar updateExistingVariant(ExtraUpdate dto) throws IOException {
        ProVar proVar = productVarRepo.findById(dto.getId()).orElseThrow(() -> new RuntimeException("Variant not found"));
        if (dto.getFile() != null) {
            proVar.setImage(s3Service.uploadToS3(dto.getFile(), "productVarImage"));
        } else if (dto.getImage() != null) {
            proVar.setImage(dto.getImage());
        }
        proVar.setPrice(new BigDecimal(dto.getPrice()));
        proVar.setStock(Integer.valueOf((dto.getStock())));
        return proVar;
    }

    private void updateVariantValues(ProVar proVar, List<ExtraValueUpdate> extraValues) {
        if (extraValues != null) {
            extraValues.forEach(value -> {
                Var var = value.getId() != null ? varRepo.findById(value.getId()).orElse(new Var()) : new Var();
                var.setKey1(value.getKey1());
                var.setValue(value.getValue());
                varRepo.save(var);
                proVar.addVar(var);
            });
        }
    }

    private String extractFilenameFromUrl2(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

}



//@PostMapping("/updateCountProduct/{id}")
//public ResponseEntity<Product> updateCountProduct(@PathVariable("id") Long id) {
//    Product product=productRepo.findById(id).get();
//    if(product.getCountProduct()==null){
//        CountProduct countProduct=new CountProduct();
//        CountPerMonth countPerMonth=new CountPerMonth();
//        countPerMonth.setCountPro(1l);
//        countPerMonth.setDateCount(YearMonth.from(LocalDate.now()));
//        countPerMonthRepo.save(countPerMonth);
//        countProduct.addCountPerMonth(countPerMonth);
//        countProductRepo.save(countProduct);
//        product.addCountProduct(countProduct);
//        productRepo.save(product);
//        return ResponseEntity.ok(product);
//    }
//    else{
//        CountProduct countProduct=product.getCountProduct();
//        List<CountPerMonth> countPerMonths=countProduct.getCountPerMonths();
//        for(CountPerMonth countPerMonth:countPerMonths){
//            if(countPerMonth.getDateCount().equals(YearMonth.from(LocalDate.now()))){
//                countPerMonth.setCountPro(countPerMonth.getCountPro()+1);
//                countPerMonthRepo.save(countPerMonth);
//                return ResponseEntity.ok(product);
//            }
//        }
//
//        CountPerMonth countPerMonth=new CountPerMonth();
//        countPerMonth.setCountPro(1l);
//        countPerMonth.setDateCount(YearMonth.from(LocalDate.now()));
//        countPerMonthRepo.save(countPerMonth);
//        countProduct.addCountPerMonth(countPerMonth);
//        countProductRepo.save(countProduct);
//        product.addCountProduct(countProduct);
//        productRepo.save(product);
//
//        return ResponseEntity.ok(product);
//    }
//
//}

//@GetMapping("/top10ProductSearchPerMonth")
//public ResponseEntity<List<Product>> top10ProductSearchPerMonth(@RequestParam("date") YearMonth date) {
//    Pageable pageable = PageRequest.of(0, 10); // Trang đầu tiên, 10 kết quả
//    List<Product>   products= productRepo.findTop10ProductsWithHighestCountPerMonth( date, pageable).getContent();
//    return ResponseEntity.ok(products);
//}

//@GetMapping("/getTop10ProductByElastic")
//public ResponseEntity<List<Map<String,String>>> getTop10ProductByElastic(@RequestParam("texts") String texts) {
//    return ResponseEntity.ok(elasticsearchService.top10ProductCountSearch(texts));
//}