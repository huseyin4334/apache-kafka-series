package com.example.productservice.rest;

import com.example.productservice.models.CreateProductRestModel;
import com.example.productservice.models.ErrorMessage;
import com.example.productservice.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

    final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody CreateProductRestModel product) {
        try {
            String productId = productService.createProductAsynchronously(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(productId);
        } catch (Exception e) {
            log.error("An error occurred while creating the product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(
                            e.getMessage(),
                            "An error occurred while creating the product",
                            new Date()
                    ));
        }
    }

}
