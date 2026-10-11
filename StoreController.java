package com.example.ecommerce;

import java.math.BigDecimal;
import java.util.List;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StoreController {
    private final StoreService store;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;
    public StoreController(StoreService store, org.springframework.jdbc.core.JdbcTemplate jdbc) {
        this.store = store;
        this.jdbc = jdbc;
    }

    @ModelAttribute
    public void common(Model model, HttpSession session) {
        model.addAttribute("cartCount", store.cart(session.getId()).stream().mapToInt(CartLine::quantity).sum());
    }
    @GetMapping("/")
    public String home(@RequestParam(required=false) String q,
                       @RequestParam(required=false) String category,
                       @RequestParam(required=false) BigDecimal min,
                       @RequestParam(required=false) BigDecimal max, Model model) {
        model.addAttribute("products", store.products(q, category, min, max));
        model.addAttribute("categories", store.categories());
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("selectedCategory", category == null ? "All" : category);
        model.addAttribute("min", min == null ? "" : min);
        model.addAttribute("max", max == null ? "" : max);
        return "index";
    }
    @GetMapping("/products/{id}")
    public String details(@PathVariable long id, Model model) {
        model.addAttribute("product", store.product(id)); return "product";
    }
    @PostMapping("/cart")
    public String add(@RequestParam long productId, @RequestParam(defaultValue="1") int quantity,
                      HttpSession session, RedirectAttributes flash) {
        store.addToCart(session.getId(), productId, quantity);
        flash.addFlashAttribute("message", "Product added to your cart.");
        return "redirect:/";
    }
    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        List<CartLine> lines = store.cart(session.getId());
        model.addAttribute("lines", lines); model.addAttribute("total", store.cartTotal(session.getId()));
        return "cart";
    }
    @PostMapping("/cart/{id}/update")
    public String update(@PathVariable long id, @RequestParam int quantity, HttpSession session, RedirectAttributes flash) {
        store.updateCart(session.getId(), id, quantity);
        flash.addFlashAttribute("message", "Cart updated."); return "redirect:/cart";
    }
    @PostMapping("/cart/{id}/remove")
    public String remove(@PathVariable long id, HttpSession session, RedirectAttributes flash) {
        store.removeCart(session.getId(), id);
        flash.addFlashAttribute("message", "Item removed."); return "redirect:/cart";
    }
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        if (store.cart(session.getId()).isEmpty()) return "redirect:/cart";
        model.addAttribute("total", store.cartTotal(session.getId())); return "checkout";
    }
    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String name, @RequestParam String email,
                             @RequestParam String address, HttpSession session, Model model) {
        long orderId = store.checkout(session.getId(), name, email, address);
        model.addAttribute("orderId", orderId); return "success";
    }
    @GetMapping("/signup")
    public String signup() { return "signup"; }
    @PostMapping("/signup")
    public String register(@RequestParam String name, @RequestParam String email,
                           @RequestParam String password, RedirectAttributes flash) {
        if (name.isBlank() || email.isBlank() || password.length() < 8) {
            flash.addFlashAttribute("error", "Enter all details. Password must be at least 8 characters.");
            return "redirect:/signup";
        }
        try {
            String hash = org.springframework.security.crypto.bcrypt.BCrypt
                .hashpw(password, org.springframework.security.crypto.bcrypt.BCrypt.gensalt());
            storeUser(name, email, hash);
            flash.addFlashAttribute("message", "Account created. Demo signup only; login is not enabled in this starter.");
            return "redirect:/signup";
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            flash.addFlashAttribute("error", "This email is already registered.");
            return "redirect:/signup";
        }
    }
    private void storeUser(String name, String email, String hash) {
        // Passwords are hashed with BCrypt. This starter intentionally does not implement session login.
        jdbc.update("INSERT INTO app_users(name,email,password_hash) VALUES(?,?,?)", name.trim(), email.trim().toLowerCase(), hash);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public String badRequest(IllegalArgumentException ex, RedirectAttributes flash) {
        flash.addFlashAttribute("error", ex.getMessage()); return "redirect:/";
    }
}
