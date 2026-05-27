package com.asra.asraopticals.controller;

import com.asra.asraopticals.model.Product;
import com.asra.asraopticals.repository.OfferRepository;
import com.asra.asraopticals.repository.ProductRepository;
import com.asra.asraopticals.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class ShopController {

    @Autowired private ProductRepository productRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private OfferRepository offerRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", productRepository.findByActiveTrue());
        // Load active offers for home page banner/section
        model.addAttribute("offers", offerRepository.findByActiveTrueOrderByIdDesc());
        return "shop";
    }

    @GetMapping("/shop")
    public String shopFiltered(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String shape,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String offers,
            Model model) {

        List<Product> products;
        String pageTitle = "All Products";

        if ("true".equals(offers)) {
            // Show all products that have active offers
            var activeOffers = offerRepository.findByActiveTrueOrderByIdDesc();
            products = new ArrayList<>();
            for (var o : activeOffers) {
                if (o.getProduct() != null && !products.contains(o.getProduct())) {
                    products.add(o.getProduct());
                }
            }
            pageTitle = "🔥 Special Offers";
            model.addAttribute("activeOffers", activeOffers);
        } else if (keyword != null && !keyword.isBlank()) {
            products = productRepository.searchActive(keyword.trim());
            pageTitle = "Results for \"" + keyword.trim() + "\"";
        } else if (category != null && !category.isBlank()) {
            products = productRepository.findByCategoryAndActiveTrue(category);
            pageTitle = category;
        } else if (shape != null && !shape.isBlank()) {
            products = productRepository.findByShapeAndActiveTrue(shape);
            pageTitle = shape + " Frames";
        } else if (gender != null && !gender.isBlank()) {
            products = productRepository.findByGenderAndActiveTrue(gender);
            pageTitle = gender + "'s Collection";
        } else {
            products = productRepository.findByActiveTrue();
        }

        model.addAttribute("products", products);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("totalCount", products.size());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedGender", gender);
        model.addAttribute("selectedShape", shape);
        model.addAttribute("keyword", keyword);
        model.addAttribute("isOffers", "true".equals(offers));
        return "shop-filtered";
    }

    @GetMapping("/product/{id}")
    public String productDetails(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        List<String> imageList;
        if (product.getImages() != null && !product.getImages().isBlank()) {
            imageList = Arrays.asList(product.getImages().split(","));
        } else if (product.getImageName() != null) {
            imageList = List.of(product.getImageName());
        } else {
            imageList = new ArrayList<>();
        }

        List<Product> related = productRepository.findByCategoryAndActiveTrue(
                product.getCategory() != null ? product.getCategory() : "");
        related.removeIf(p -> p.getId().equals(id));
        if (related.size() > 4) related = related.subList(0, 4);

        var reviews = reviewRepository.findByProductIdAndApprovedTrue(id);
        Double avgRating = reviewRepository.avgRatingByProductId(id);
        Long reviewCount = reviewRepository.countByProductId(id);

        // Check if product has an active offer
        var allOffers = offerRepository.findByActiveTrueOrderByIdDesc();
        var productOffer = allOffers.stream()
                .filter(o -> o.getProduct() != null && o.getProduct().getId().equals(id))
                .findFirst().orElse(null);

        model.addAttribute("product", product);
        model.addAttribute("images", imageList);
        model.addAttribute("related", related);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating != null ? String.format("%.1f", avgRating) : "0.0");
        model.addAttribute("reviewCount", reviewCount != null ? reviewCount : 0);
        model.addAttribute("productOffer", productOffer);
        return "product-details";
    }
}