package com.example.ecommerce;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreService {
    private final JdbcTemplate jdbc;
    public StoreService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private final RowMapper<Product> productMapper = (rs, n) -> new Product(
        rs.getLong("id"), rs.getString("name"), rs.getString("category"),
        rs.getString("description"), rs.getBigDecimal("price"),
        rs.getString("image_url"), rs.getInt("stock"));

    public List<Product> products(String q, String category, BigDecimal min, BigDecimal max) {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        java.util.List<Object> args = new java.util.ArrayList<>();
        if (q != null && !q.isBlank()) { sql.append(" AND (name LIKE ? OR description LIKE ?)"); args.add("%"+q.trim()+"%"); args.add("%"+q.trim()+"%"); }
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All")) { sql.append(" AND category=?"); args.add(category); }
        if (min != null) { sql.append(" AND price>=?"); args.add(min); }
        if (max != null) { sql.append(" AND price<=?"); args.add(max); }
        sql.append(" ORDER BY id DESC");
        return jdbc.query(sql.toString(), productMapper, args.toArray());
    }
    public Product product(long id) {
        return jdbc.query("SELECT * FROM products WHERE id=?", productMapper, id)
            .stream().findFirst().orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }
    public List<String> categories() {
        return jdbc.queryForList("SELECT DISTINCT category FROM products ORDER BY category", String.class);
    }
    @Transactional
    public void addToCart(String session, long productId, int quantity) {
        if (quantity < 1 || quantity > 99) throw new IllegalArgumentException("Quantity must be between 1 and 99");
        product(productId);
        jdbc.update("INSERT INTO cart_items(session_id,product_id,quantity) VALUES(?,?,?) " +
            "ON DUPLICATE KEY UPDATE quantity=LEAST(quantity+VALUES(quantity),99)", session, productId, quantity);
    }
    public List<CartLine> cart(String session) {
        return jdbc.query("SELECT c.id,p.id product_id,p.name,p.image_url,p.price,c.quantity," +
            "(p.price*c.quantity) line_total FROM cart_items c JOIN products p ON p.id=c.product_id " +
            "WHERE c.session_id=? ORDER BY c.id DESC", (rs,n) -> new CartLine(
            rs.getLong("id"),rs.getLong("product_id"),rs.getString("name"),rs.getString("image_url"),
            rs.getBigDecimal("price"),rs.getInt("quantity"),rs.getBigDecimal("line_total")), session);
    }
    public BigDecimal cartTotal(String session) {
        BigDecimal total = jdbc.queryForObject("SELECT COALESCE(SUM(p.price*c.quantity),0) FROM cart_items c " +
            "JOIN products p ON p.id=c.product_id WHERE c.session_id=?", BigDecimal.class, session);
        return total == null ? BigDecimal.ZERO : total;
    }
    public void updateCart(String session, long cartId, int quantity) {
        if (quantity < 1 || quantity > 99) throw new IllegalArgumentException("Quantity must be between 1 and 99");
        jdbc.update("UPDATE cart_items SET quantity=? WHERE id=? AND session_id=?", quantity, cartId, session);
    }
    public void removeCart(String session, long cartId) {
        jdbc.update("DELETE FROM cart_items WHERE id=? AND session_id=?", cartId, session);
    }
    @Transactional
    public long checkout(String session, String name, String email, String address) {
        List<CartLine> lines = cart(session);
        if (lines.isEmpty()) throw new IllegalArgumentException("Your cart is empty");
        BigDecimal total = cartTotal(session);
        jdbc.update("INSERT INTO orders(session_id,customer_name,email,address,total) VALUES(?,?,?,?,?)",
            session, name.trim(), email.trim(), address.trim(), total);
        Long orderId = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        for (CartLine line : lines) {
            jdbc.update("INSERT INTO order_items(order_id,product_id,product_name,quantity,unit_price) VALUES(?,?,?,?,?)",
                orderId, line.productId(), line.name(), line.quantity(), line.price());
        }
        jdbc.update("DELETE FROM cart_items WHERE session_id=?", session);
        return orderId == null ? 0 : orderId;
    }
}
