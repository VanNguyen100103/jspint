package com.ecommerce.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.backend.entity.Category;

public interface CategoryRepo extends JpaRepository<Category, Long> {
}
