package com.asra.asraopticals.controller;

import com.asra.asraopticals.model.Product;
import com.asra.asraopticals.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductRepository productRepository;

    /** Makes cart count available on every page automatically */
    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Map<Long, Integer> cart = getCart(session);
        List<Product> products = new ArrayList<>();
        double total = 0;

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            productRepository.findById(entry.getKey()).ifPresent(p -> {
                products.add(p);
            });
        }

        for (Product p : products) {
            total += p.getPrice() * cart.getOrDefault(p.getId(), 0);
        }

        model.addAttribute("products", products);
        model.addAttribute("cart", cart);
        model.addAttribute("total", String.format("%.2f", total));
        return "cart";
    }

    @PostMapping("/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session,
                            @RequestHeader(value = "Referer", defaultValue = "/") String referer) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null && product.isActive() && product.getStock() > 0) {
            Map<Long, Integer> cart = getCart(session);
            int current = cart.getOrDefault(id, 0);
            // Don't add more than stock allows
            if (current < product.getStock()) {
                cart.put(id, current + 1);
                session.setAttribute("cart", cart);
            }
        }
        return "redirect:" + referer;
    }

    @GetMapping("/remove/{id}")
    public String removeItem(@PathVariable Long id, HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        cart.remove(id);
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @GetMapping("/increase/{id}")
    public String increase(@PathVariable Long id, HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        Product product = productRepository.findById(id).orElse(null);
        if (product != null && cart.containsKey(id)) {
            int current = cart.get(id);
            if (current < product.getStock()) {
                cart.put(id, current + 1);
                session.setAttribute("cart", cart);
            }
        }
        return "redirect:/cart";
    }

    @GetMapping("/decrease/{id}")
    public String decrease(@PathVariable Long id, HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        if (cart.containsKey(id)) {
            int qty = cart.get(id);
            if (qty > 1) cart.put(id, qty - 1);
            else cart.remove(id);
            session.setAttribute("cart", cart);
        }
        return "redirect:/cart";
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }
}