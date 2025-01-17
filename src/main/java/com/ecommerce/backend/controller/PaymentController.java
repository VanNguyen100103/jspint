package com.ecommerce.backend.controller;

import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.utils.MomoConfig;

@RestController
@RequestMapping("/api/payment")
@AllArgsConstructor
public class PaymentController {

    private final MomoConfig momoConfig;


    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestParam double amount,
                                                             @RequestParam String orderId,
                                                             @RequestParam String userId) {
        try {
            Map<String, Object> response = momoConfig.createMomoPayment(amount, orderId, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<Order> handleMomoCallback(@RequestParam Map<String, String> query) {
        try {
            Order order = momoConfig.handleMomoCallback(query);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}


