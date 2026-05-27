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
public class WishlistController {

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/wishlist/add/{id}")
    public String add(@PathVariable Long id, HttpSession session,
                      @RequestHeader(value = "Referer", defaultValue = "/") String referer) {
        List<Long> wishlist = getWishlist(session);
        if (!wishlist.contains(id)) wishlist.add(id);
        session.setAttribute("wishlist", wishlist);
        return "redirect:" + referer;
    }

    @GetMapping("/wishlist/remove/{id}")
    public String remove(@PathVariable Long id, HttpSession session) {
        List<Long> wishlist = getWishlist(session);
        wishlist.remove(id);
        session.setAttribute("wishlist", wishlist);
        return "redirect:/wishlist";
    }

    @GetMapping("/wishlist")
    public String view(HttpSession session, Model model) {
        List<Long> ids = getWishlist(session);
        List<Product> products = ids.isEmpty()
                ? new ArrayList<>()
                : productRepository.findAllById(ids);
        model.addAttribute("products", products);
        return "wishlist";
    }

    @SuppressWarnings("unchecked")
    private List<Long> getWishlist(HttpSession session) {
        List<Long> wl = (List<Long>) session.getAttribute("wishlist");
        if (wl == null) { wl = new ArrayList<>(); session.setAttribute("wishlist", wl); }
        return wl;
    }
}