package com.ecommerce.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.backend.entity.User;

public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
