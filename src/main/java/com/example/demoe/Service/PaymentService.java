package com.example.demoe.Service;

import com.example.demoe.Config.VnPayConfig;
import com.example.demoe.Dto.OrderPaid.OrderPaidDto;
import com.example.demoe.Dto.OrderPaid.OrderPaidItemDto;
import com.example.demoe.Dto.OrderPaid.ProVarDto;
import com.example.demoe.Dto.PaymentDto;
import com.example.demoe.Entity.Order.Order1;
import com.example.demoe.Entity.Order.OrderPaid;
import com.example.demoe.Entity.User;
import com.example.demoe.Repository.*;
import com.example.demoe.Util.VnPayUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final VnPayConfig vnPayConfig;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductVarRepo productVarRepo;
    @Autowired
    private Order1Repo order1Repo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private OrderPaidRepo orderPaidRepo;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private S3Service s3Service;
    public PaymentDto.VNPayResponse createVnPayPayment(HttpServletRequest request) {
        long amount = Integer.parseInt(request.getParameter("amount")) * 100L;
        String bankCode = request.getParameter("bankCode");
        String language=request.getParameter("language");
        String ref=(String)request.getParameter("ref");
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));
        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
            vnpParamsMap.put("vnp_Locale", language);
            vnpParamsMap.put("vnp_TxnRef", ref);
        }
        vnpParamsMap.put("vnp_IpAddr", VnPayUtil.getIpAddress(request));
        //build query url
        String queryUrl = VnPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VnPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VnPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        return PaymentDto.VNPayResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl).build();
    }

    public void transfer( Long order1Id) throws JsonProcessingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        Optional<Order1> order1Opt = order1Repo.findById(order1Id);
        List<OrderPaidItemDto> orderPaidItemDtos = new ArrayList<>();
        order1Opt.get().getOrder1ItemList().forEach(order1Item -> {
            List<ProVarDto> proVarDtos = new ArrayList<>();
            order1Item.getProVar().getVars().forEach(var -> {
                ProVarDto proVarDto=ProVarDto.builder()
                        .id(var.getId())
                        .key1(var.getKey1())
                        .value(var.getValue())
                        .build();
                proVarDtos.add(proVarDto);
            });
            OrderPaidItemDto orderPaidItemDto = OrderPaidItemDto.builder()
                    .proVarDtos(proVarDtos)
                    .productName(order1Item.getProduct().getProductName())
                    .productId(order1Item.getProduct().getId())
                    .quantity(order1Item.getQuantity())
                    .id(order1Item.getId())
                    .productVarId(order1Item.getProVar().getId())
                    .proVarDtos(proVarDtos)
                    .image(s3Service.getPresignedUrl(order1Item.getProVar().getImage()))
                    .build();
            orderPaidItemDtos.add(orderPaidItemDto);

        });
        OrderPaidDto orderPaidDto = OrderPaidDto.builder()
                .id(order1Id)
                .order1Date(order1Opt.get().getOrder1Date())
                .orderPaidItemDtos(orderPaidItemDtos)
                .txnRep(order1Opt.get().getTxnRep())
                .price(order1Opt.get().getPrice())
                .status(order1Opt.get().getStatus())
                .build();

        if (order1Opt.isPresent()) {
            // Chuyển đổi đối tượng Order1 sang JSON
            String order1Json = objectMapper.writeValueAsString(orderPaidDto);
//            Map<String, Object> order1Map = objectMapper.convertValue(order1Opt.get());
//
//            Map<String, Object> jsonMap = new HashMap<>();
//            jsonMap.put(String.valueOf(order1Opt.get().getId()), order1Json);
//            // Tạo một bản ghi mới trong Order2
            OrderPaid order2 = new OrderPaid();
            order2.setPropertiesArray(order1Json);
            order2.setCreatedAt(LocalDateTime.now());
            orderPaidRepo.save(order2);
            user1.get().addOrderPaid(order2);

            userRepo.save(user1.get());
        }
    }
}