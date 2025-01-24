package com.example.notificationservice.repository;

import com.example.notificationservice.model.CreatedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatedProductRepository extends JpaRepository<CreatedProduct, String> {

    boolean isExistsById(String id);
}
