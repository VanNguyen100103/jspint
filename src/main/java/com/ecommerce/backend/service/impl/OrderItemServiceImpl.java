package com.ecommerce.backend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.ecommerce.backend.dto.OrderItemDto;
import com.ecommerce.backend.dto.OrderRequest;
import com.ecommerce.backend.dto.Response;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.exception.NotFoundException;
import com.ecommerce.backend.mapper.EntityDtoMapper;
import com.ecommerce.backend.repository.OrderItemRepo;
import com.ecommerce.backend.repository.OrderRepo;
import com.ecommerce.backend.repository.ProductRepo;
import com.ecommerce.backend.service.interf.OrderItemService;
import com.ecommerce.backend.service.interf.UserService;
import com.ecommerce.backend.specification.OrderItemSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemServiceImpl implements OrderItemService {


    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final ProductRepo productRepo;
    private final UserService userService;
    private final EntityDtoMapper entityDtoMapper;


    @Override
    public Response placeOrder(OrderRequest orderRequest) {
    
        // Get the logged-in user
        User user = userService.getLoginUser();
    
        // Map OrderRequest items to OrderItem entities
        List<OrderItem> orderItems = orderRequest.getItems().stream().map(orderItemRequest -> {
            Product product = productRepo.findById(orderItemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product Not Found"));
    
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(orderItemRequest.getQuantity());
            orderItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(orderItemRequest.getQuantity()))); // Set price
            orderItem.setStatus(OrderStatus.PENDING);
            orderItem.setUser(user); // Set the user for the OrderItem (if needed elsewhere)
            return orderItem;
        }).collect(Collectors.toList());
    
        // Calculate the total price
        BigDecimal totalPrice = orderItems.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        // Create the Order
        Order order = new Order();
        order.setUser(user); // Assign the user to the order
        order.setOrderItemList(orderItems); // Associate items with the order
        order.setTotalPrice(totalPrice); // Set total price
    
        // Associate each OrderItem with the parent Order
        orderItems.forEach(orderItem -> orderItem.setOrder(order));
    
        // Save the Order
        orderRepo.save(order);
    
        return Response.builder()
                .status(200)
                .message("Order was successfully placed")
                .build();
    }
    

    @Override
    public Response updateOrderItemStatus(Long orderItemId, String status) {
        OrderItem orderItem = orderItemRepo.findById(orderItemId)
                .orElseThrow(()-> new NotFoundException("Order Item not found"));

        orderItem.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        orderItemRepo.save(orderItem);
        return Response.builder()
                .status(200)
                .message("Order status updated successfully")
                .build();
    }

    @Override
    public Response filterOrderItems(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Long itemId, Pageable pageable) {
        Specification<OrderItem> spec = Specification.where(OrderItemSpecification.hasStatus(status))
                .and(OrderItemSpecification.createdBetween(startDate, endDate))
                .and(OrderItemSpecification.hasItemId(itemId));

        Page<OrderItem> orderItemPage = orderItemRepo.findAll(spec, pageable);

        if (orderItemPage.isEmpty()){
            throw new NotFoundException("No Order Found");
        }
        List<OrderItemDto> orderItemDtos = orderItemPage.getContent().stream()
                .map(entityDtoMapper::mapOrderItemToDtoPlusProductAndUser)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .orderItemList(orderItemDtos)
                .totalPage(orderItemPage.getTotalPages())
                .totalElement(orderItemPage.getTotalElements())
                .build();
    }

}
