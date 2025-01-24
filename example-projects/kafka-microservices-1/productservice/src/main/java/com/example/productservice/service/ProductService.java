package com.example.productservice.service;

import com.example.productservice.models.CreateProductRestModel;

import java.util.concurrent.ExecutionException;

public interface ProductService {
    String createProductAsynchronously(CreateProductRestModel product);
    String createProductSynchronously(CreateProductRestModel product);
    String createProductSynchronouslyWithGetMethod(CreateProductRestModel product) throws ExecutionException, InterruptedException;
}
