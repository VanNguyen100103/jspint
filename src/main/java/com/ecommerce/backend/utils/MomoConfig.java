package com.ecommerce.backend.utils;

import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.repository.OrderRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Component
public class MomoConfig {
    private final ObjectMapper objectMapper;
    public final String accessKey = "F8BBA842ECF85";
    public final String secretKey = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    public final String partnerCode = "MOMO";
    public final String orderInfo = "pay with MoMo";
    public final String redirectUrl = "http://localhost:8080/api/payment/callback";
    public final String ipnUrl = "https://2b24-14-169-31-246.ngrok-free.app/api/v1/callback";
    public final String requestType = "payWithMethod";
    public final Boolean autoCapture = true;
    public final String lang = "vi";
    private final OrderRepo orderRepository;

    public Map<String, Object> createMomoPayment(double totalPrice, String orderId, String userId) throws Exception {
        String requestId = partnerCode + System.currentTimeMillis();
        String extraData = "";
        try {
            extraData = Base64.getEncoder().encodeToString(
                    objectMapper.writeValueAsString(Map.of("orderId", orderId, "user", userId))
                            .getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error encoding extraData", e);
            extraData = "";
        }

        // Raw Signature String - sorted alphabetically by parameter name
        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + String.valueOf(Math.round(totalPrice)) +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + requestId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        log.info("Raw signature: {}", rawSignature);

        // Generate Signature
        String signature = createMomoSignature(rawSignature);
        log.info("Generated signature: {}", signature);

        // Request Body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", partnerCode);
        requestBody.put("partnerName", "Test");
        requestBody.put("storeId", "MomoTestStore");
        requestBody.put("requestId", requestId);
        requestBody.put("amount", Math.round(totalPrice));
        requestBody.put("orderId", requestId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", redirectUrl);
        requestBody.put("ipnUrl", ipnUrl);
        requestBody.put("lang", lang);
        requestBody.put("requestType", requestType);
        requestBody.put("autoCapture", autoCapture);
        requestBody.put("extraData", extraData);
        requestBody.put("signature", signature);

        log.info("Request Body: {}", objectMapper.writeValueAsString(requestBody));

        // Send POST request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://test-payment.momo.vn/v2/gateway/api/create"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Response: {}", response.body());
        return objectMapper.readValue(response.body(), Map.class);
    }

    private String createMomoSignature(String rawSignature) throws Exception {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secretKeySpec);
        byte[] hmacData = hmacSha256.doFinal(rawSignature.getBytes(StandardCharsets.UTF_8));
        return new String(Hex.encode(hmacData)).toLowerCase();
    }

    public Order handleMomoCallback(Map<String, String> query) throws Exception {
        String status = "Shipping";
        String rawData = query.get("extraData");

        // Parse extraData (assume it's a JSON string)
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> parseData = objectMapper.readValue(rawData, new TypeReference<>() {});
        String orderId = (String) parseData.get("orderId");

        // Update the order
        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setOrderStatus(status);
        order.setPaymentStatus(query.get("message"));
        order.setDeliveryDate(new Date(System.currentTimeMillis() + 48 * 60 * 60 * 1000L));
        order.setDeliveryExpectDate(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L));
        orderRepository.save(order);

        return order;
    }
}
