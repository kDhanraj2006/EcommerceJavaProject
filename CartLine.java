package com.example.ecommerce;

import java.math.BigDecimal;

public record CartLine(long id, long productId, String name, String imageUrl,
                       BigDecimal price, int quantity, BigDecimal lineTotal) {}
