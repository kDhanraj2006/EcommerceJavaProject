package com.example.ecommerce;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProductApiController {
    private final StoreService store;
    public ProductApiController(StoreService store) { this.store = store; }

    @GetMapping("/products")
    public List<Product> products(@RequestParam(required=false) String q,
                                  @RequestParam(required=false) String category,
                                  @RequestParam(required=false) BigDecimal min,
                                  @RequestParam(required=false) BigDecimal max) {
        return store.products(q, category, min, max);
    }

    @GetMapping("/products/{id}")
    public Product product(@PathVariable long id) {
        return store.product(id);
    }
}
