package com.ecommerce.backend.service.interf;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.backend.dto.Response;

public interface ProductService {

    Response createProduct(Long categoryId, MultipartFile image, String name, String description, BigDecimal price);
    Response updateProduct(Long productId, Long categoryId, MultipartFile image, String name, String description, BigDecimal price);
    Response deleteProduct(Long productId);
    Response getProductById(Long productId);
    Response getAllProducts(int page, int size, String sortBy, String sortDir);
    Response getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDir);
    Response searchProduct(String searchValue, int page, int size, String sortBy, String sortDir);
}
