package com.example.productservice.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductRestModel {
    String name;
    BigDecimal price;
    Integer quantity;
}
