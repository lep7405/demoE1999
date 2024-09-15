package com.example.demoe.Controller;

import com.example.demoe.Dto.PaymentDto;
import com.example.demoe.Entity.Order.Order1;
import com.example.demoe.Entity.Order.Order1Item;
import com.example.demoe.Entity.product.ProVar;
import com.example.demoe.Repository.Order1Repo;
import com.example.demoe.Repository.ProductVarRepo;
import com.example.demoe.Service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})

public class PaymentController {
   @Autowired
   private PaymentService paymentService;
    @Autowired
    private Order1Repo order1Repo;
    @Autowired
    private ProductVarRepo proVarRepo;
    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/vn-pay")
    public ResponseObject<PaymentDto.VNPayResponse> pay(HttpServletRequest request) {

        return new ResponseObject<>(HttpStatus.OK, "Success", paymentService.createVnPayPayment(request));
    }
}
