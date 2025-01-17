package com.ecommerce.backend.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.backend.dto.ProductDto;
import com.ecommerce.backend.dto.Response;
import com.ecommerce.backend.entity.Category;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.exception.NotFoundException;
import com.ecommerce.backend.mapper.EntityDtoMapper;
import com.ecommerce.backend.repository.CategoryRepo;
import com.ecommerce.backend.repository.ProductRepo;
import com.ecommerce.backend.service.interf.ProductService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final EntityDtoMapper entityDtoMapper;
   



    @Override
    public Response createProduct(Long categoryId, MultipartFile image, String name, String description, BigDecimal price) {
        Category category = categoryRepo.findById(categoryId).orElseThrow(()-> new NotFoundException("Category not found"));
   

        Product product = new Product();
        product.setCategory(category);
        product.setPrice(price);
        product.setName(name);
        product.setDescription(description);


        productRepo.save(product);
        return Response.builder()
                .status(200)
                .message("Product successfully created")
                .build();
    }

    @Override
    public Response updateProduct(Long productId, Long categoryId, MultipartFile image, String name, String description, BigDecimal price) {
        Product product = productRepo.findById(productId).orElseThrow(()-> new NotFoundException("Product Not Found"));

        Category category = null;
        String productImageUrl = null;

        if(categoryId != null ){
             category = categoryRepo.findById(categoryId).orElseThrow(()-> new NotFoundException("Category not found"));
        }
      

        if (category != null) product.setCategory(category);
        if (name != null) product.setName(name);
        if (price != null) product.setPrice(price);
        if (description != null) product.setDescription(description);
        if (productImageUrl != null) product.setImageUrl(productImageUrl);

        productRepo.save(product);
        return Response.builder()
                .status(200)
                .message("Product updated successfully")
                .build();

    }

    @Override
    public Response deleteProduct(Long productId) {
        Product product = productRepo.findById(productId).orElseThrow(()-> new NotFoundException("Product Not Found"));
        productRepo.delete(product);

        return Response.builder()
                .status(200)
                .message("Product deleted successfully")
                .build();
    }

    @Override
    public Response getProductById(Long productId) {
        Product product = productRepo.findById(productId).orElseThrow(()-> new NotFoundException("Product Not Found"));
        ProductDto productDto = entityDtoMapper.mapProductToDtoBasic(product);

        return Response.builder()
                .status(200)
                .product(productDto)
                .build();
    }

    @Override
    public Response getAllProducts(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Product> productPage = productRepo.findAll(pageable);

        List<ProductDto> productList = productPage.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .productList(productList)
                .totalPage(productPage.getTotalPages())
                .totalElement(productPage.getTotalElements())
                .build();
    }

    @Override
    public Response getProductsByCategory(Long categoryId, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<Product> productPage = productRepo.findByCategoryId(categoryId, pageable);

        if (productPage.isEmpty()) {
            throw new NotFoundException("No Products found for this category");
        }

        List<ProductDto> productDtoList = productPage.stream()
                .map(entityDtoMapper::mapProductToDtoBasic)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .productList(productDtoList)
                .totalPage(productPage.getTotalPages())
                .totalElement(productPage.getTotalElements())
                .build();
    }

    
    
    @Override
public Response searchProduct(String searchValue, int page, int size, String sortBy, String sortDir) {
    // Tạo Pageable cho phân trang và sắp xếp
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
    
    // Thực hiện tìm kiếm sản phẩm
    Page<Product> productPage = productRepo.findByNameContainingOrDescriptionContaining(searchValue, searchValue, pageable);

    if (productPage.isEmpty()) {
        throw new NotFoundException("No Products Found");
    }

    // Chuyển đổi thành ProductDto
    List<ProductDto> productDtoList = productPage.stream()
            .map(entityDtoMapper::mapProductToDtoBasic)
            .collect(Collectors.toList());

    return Response.builder()
            .status(200)
            .productList(productDtoList)
            .totalPage(productPage.getTotalPages())
            .totalElement(productPage.getTotalElements())
            .build();
}
}
