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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    @Autowired
    private Order1Repo order1Repo;
    @Autowired
    private ProductVarRepo proVarRepo;
    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/vn-pay")
    public ResponseObject<PaymentDto.VNPayResponse> pay(HttpServletRequest request) {

        return new ResponseObject<>(HttpStatus.OK, "Success", paymentService.createVnPayPayment(request));
    }
    @GetMapping("/vn-pay-callback")
    public ResponseObject<PaymentDto.VNPayResponse> payCallbackHandler(HttpServletRequest request) {
        String status = request.getParameter("vnp_ResponseCode");
        String content = request.getParameter("vnp_OrderInfo");
        String ref = request.getParameter("vnp_TxnRef");
        if (status.equals("00")) {
            Order1 order1=order1Repo.findById(Long.parseLong(ref)).get();
            order1.setStatus("Paid");
            order1Repo.save(order1);
            for(Order1Item order1Item:order1.getOrder1ItemList()) {
                ProVar proVar = order1Item.getProVar();
                proVar.setStock(proVar.getStock() - order1Item.getQuantity());
//                proVar.setSold(proVar.getSold() + order1Item.getQuantity());
                proVarRepo.save(proVar);
            }
            return new ResponseObject<>(HttpStatus.OK, "Success", new PaymentDto.VNPayResponse("00", "Success", content));
        } else {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Failed", null);
        }
    }
}
