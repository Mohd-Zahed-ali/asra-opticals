package com.asra.asraopticals.controller;

import com.asra.asraopticals.model.Product;
import com.asra.asraopticals.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Arrays;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductRepository productRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @GetMapping("/add-product")
    public String addProductPage(Model model) {
        model.addAttribute("product", new Product());
        return "admin-add-product";
    }

    @PostMapping("/save-product")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam(value = "imageFiles", required = false) MultipartFile[] files,
                              RedirectAttributes attrs) throws IOException {

        // Validate at least one image is uploaded
        boolean hasImage = files != null && Arrays.stream(files).anyMatch(f -> !f.isEmpty());
        if (!hasImage) {
            attrs.addFlashAttribute("error", "⚠️ Please upload at least one product image. Image is required.");
            attrs.addFlashAttribute("product", product);
            return "redirect:/admin/add-product";
        }

        ensureUploadDir();
        List<String> imageNames = new ArrayList<>();

        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String filename = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
                    file.transferTo(new File(UPLOAD_DIR + filename));
                    imageNames.add(filename);
                }
            }
        }

        if (!imageNames.isEmpty()) {
            product.setImageName(imageNames.get(0));
            product.setImages(String.join(",", imageNames));
        }

        productRepository.save(product);
        attrs.addFlashAttribute("success", "Product saved successfully!");
        return "redirect:/admin/add-product";
    }

    @GetMapping("/products")
    public String viewProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        // Low stock alert (≤ 5)
        model.addAttribute("lowStock", productRepository.findByStockLessThanAndActiveTrue(6));
        return "admin-products";
    }

    @GetMapping("/edit-product/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        productRepository.findById(id).ifPresent(p -> model.addAttribute("product", p));
        return "admin-edit-product";
    }

    @PostMapping("/update-product")
    public String updateProduct(@ModelAttribute Product product,
                                @RequestParam(value = "imageFile", required = false) MultipartFile file,
                                RedirectAttributes attrs) throws IOException {

        // Preserve existing images if no new file uploaded
        Product existing = productRepository.findById(product.getId()).orElse(null);
        if (existing != null) {
            if (file == null || file.isEmpty()) {
                product.setImageName(existing.getImageName());
                product.setImages(existing.getImages());
            } else {
                ensureUploadDir();
                String filename = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
                file.transferTo(new File(UPLOAD_DIR + filename));
                product.setImageName(filename);
                product.setImages(filename);
            }
        }

        productRepository.save(product);
        attrs.addFlashAttribute("success", "Product updated!");
        return "redirect:/admin/products";
    }

    @GetMapping("/delete-product/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes attrs) {
        productRepository.findById(id).ifPresent(product -> {
            if (product.getOrderItems() == null || product.getOrderItems().isEmpty()) {
                productRepository.delete(product);
                attrs.addFlashAttribute("success", "Product deleted.");
            } else {
                product.setActive(false);
                productRepository.save(product);
                attrs.addFlashAttribute("info", "Product has orders — disabled instead of deleted.");
            }
        });
        return "redirect:/admin/products";
    }

    @GetMapping("/toggle-product/{id}")
    public String toggleProduct(@PathVariable Long id) {
        productRepository.findById(id).ifPresent(p -> {
            p.setActive(!p.isActive());
            productRepository.save(p);
        });
        return "redirect:/admin/products";
    }

    private void ensureUploadDir() {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    private String sanitize(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}