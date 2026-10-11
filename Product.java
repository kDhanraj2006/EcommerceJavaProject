package com.example.ecommerce;

import java.math.BigDecimal;

public record Product(long id, String name, String category, String description,
                      BigDecimal price, String imageUrl, int stock) {}
