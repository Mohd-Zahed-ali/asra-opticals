package com.asra.asraopticals.controller;

import com.asra.asraopticals.model.Product;
import com.asra.asraopticals.model.Review;
import com.asra.asraopticals.repository.ProductRepository;
import com.asra.asraopticals.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class ReviewController {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ProductRepository productRepository;

    @PostMapping("/product/{id}/review")
    public String submitReview(@PathVariable Long id,
                               @RequestParam int rating,
                               @RequestParam String comment,
                               @RequestParam String customerName,
                               Principal principal,
                               RedirectAttributes attrs) {

        Product product = productRepository.findById(id).orElse(null);
        if (product == null) return "redirect:/";

        // One review per email per product
        String email = principal != null ? principal.getName() : "guest";
        if (!email.equals("guest") && reviewRepository.existsByProductIdAndEmail(id, email)) {
            attrs.addFlashAttribute("reviewError", "You have already reviewed this product.");
            return "redirect:/product/" + id;
        }

        Review review = new Review();
        review.setProduct(product);
        review.setRating(Math.max(1, Math.min(5, rating)));
        review.setComment(comment.trim());
        review.setCustomerName(customerName.trim());
        review.setEmail(email);
        reviewRepository.save(review);

        attrs.addFlashAttribute("reviewSuccess", "Thank you for your review! ⭐");
        return "redirect:/product/" + id;
    }

    // Admin - moderate reviews
    @GetMapping("/admin/reviews")
    public String adminReviews(org.springframework.ui.Model model) {
        model.addAttribute("reviews", reviewRepository.findAllByOrderByCreatedAtDesc());
        return "admin-reviews";
    }

    @PostMapping("/admin/reviews/toggle/{id}")
    public String toggleReview(@PathVariable Long id) {
        reviewRepository.findById(id).ifPresent(r -> {
            r.setApproved(!r.isApproved());
            reviewRepository.save(r);
        });
        return "redirect:/admin/reviews";
    }

    @PostMapping("/admin/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id) {
        reviewRepository.deleteById(id);
        return "redirect:/admin/reviews";
    }
}