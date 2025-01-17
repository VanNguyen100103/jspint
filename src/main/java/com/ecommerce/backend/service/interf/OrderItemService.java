package com.ecommerce.backend.service.interf;

import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;

import com.ecommerce.backend.dto.OrderRequest;
import com.ecommerce.backend.dto.Response;
import com.ecommerce.backend.enums.OrderStatus;

public interface OrderItemService {
    Response placeOrder(OrderRequest orderRequest);
    Response updateOrderItemStatus(Long orderItemId, String status);
    Response filterOrderItems(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Long itemId, Pageable pageable);
}
