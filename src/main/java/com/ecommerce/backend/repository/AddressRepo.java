package com.ecommerce.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.backend.entity.Address;

public interface AddressRepo extends JpaRepository<Address, Long> {
}
