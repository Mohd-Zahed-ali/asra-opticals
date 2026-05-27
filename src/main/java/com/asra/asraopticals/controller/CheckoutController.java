package com.asra.asraopticals.controller;

import com.asra.asraopticals.model.*;
import com.asra.asraopticals.repository.*;
import com.asra.asraopticals.service.CouponService;
import com.asra.asraopticals.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.asra.asraopticals.model.User;

import java.io.File;
import java.security.Principal;
import java.util.*;

@Controller
public class CheckoutController {

    @Autowired private EmailService emailService;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CouponService couponService;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/prescriptions/";

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model, Principal principal) {
        Map<Long, Integer> cart = getCart(session);
        if (cart == null || cart.isEmpty()) return "redirect:/cart";

        List<Product> products = new ArrayList<>();
        double subtotal = 0;

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            productRepository.findById(entry.getKey()).ifPresent(products::add);
        }
        for (Product p : products) {
            subtotal += p.getPrice() * cart.getOrDefault(p.getId(), 0);
        }

        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(u -> {
                model.addAttribute("userFullname", u.getFullname());
                model.addAttribute("userPhone", u.getPhone());
                model.addAttribute("userEmail", u.getEmail());
            });
        }

        model.addAttribute("products", products);
        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", String.format("%.2f", subtotal));
        model.addAttribute("total", String.format("%.2f", subtotal));
        return "checkout";
    }

    // AJAX endpoint to validate coupon
    @PostMapping("/coupon/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateCoupon(
            @RequestParam String code,
            @RequestParam double total) {

        CouponService.CouponResult result = couponService.validate(code, total);
        Map<String, Object> response = new HashMap<>();
        response.put("valid", result.valid());
        response.put("message", result.message());
        response.put("discount", result.discount());
        response.put("newTotal", total - result.discount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/place-order")
    public String placeOrder(
            @RequestParam String customerName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false) String upiReference,
            @RequestParam(required = false) MultipartFile prescription,
            Principal principal,
            HttpSession session,
            Model model) {

        Map<Long, Integer> cart = getCart(session);
        if (cart == null || cart.isEmpty()) return "redirect:/cart";

        //String email = (principal != null) ? principal.getName() : "guest";
        String email = "guest";
        if (principal != null) {
            // Try username lookup first (regular login)
            var user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
                email = user.getEmail();
            } else if (principal instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken) {
                // Google OAuth — extract real email from attributes
                Object emailAttr = oauthToken.getPrincipal().getAttributes().get("email");
                if (emailAttr != null) email = emailAttr.toString();
            } else {
                // Fallback: check if principal name looks like an email
                String name = principal.getName();
                email = name.contains("@") ? name : "guest";
            }
        }

        Order order = new Order();
        order.setCustomerName(customerName);
        order.setPhone(phone);
        order.setAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setEmail(email);
        order.setOrderNumber("ASRA-" + System.currentTimeMillis());
        order.setStatus("PENDING");
        order.setUpiReference(upiReference);

        double subtotal = 0;
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productRepository.findById(entry.getKey()).orElse(null);
            if (product != null) {
                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setQuantity(entry.getValue());
                item.setPrice(product.getPrice());
                item.setOrder(order);
                order.getItems().add(item);
                subtotal += product.getPrice() * entry.getValue();

                // Deduct stock
                int newStock = Math.max(0, product.getStock() - entry.getValue());
                product.setStock(newStock);
                if (newStock == 0) product.setActive(false);
                productRepository.save(product);
            }
        }

        // Apply coupon
        double discount = 0;
        if (couponCode != null && !couponCode.isBlank()) {
            CouponService.CouponResult result = couponService.validate(couponCode.trim(), subtotal);
            if (result.valid()) {
                discount = result.discount();
                order.setCouponCode(couponCode.toUpperCase().trim());
                order.setDiscountAmount(discount);
                couponService.incrementUsage(couponCode.trim());
            }
        }
        order.setTotalAmount(Math.max(0, subtotal - discount));

        // Save prescription
        if (prescription != null && !prescription.isEmpty()) {
            try {
                new File(UPLOAD_DIR).mkdirs();
                String filename = "RX-" + System.currentTimeMillis() + "-" + prescription.getOriginalFilename();
                prescription.transferTo(new File(UPLOAD_DIR + filename));
                order.setPrescriptionFile(filename);
            } catch (Exception e) {
                System.err.println("Prescription upload failed: " + e.getMessage());
            }
        }

        orderRepository.save(order);
        emailService.sendOrderConfirmation(email, order);

        session.removeAttribute("cart");
        session.removeAttribute("appliedCoupon");
        model.addAttribute("orderNumber", order.getOrderNumber());
        model.addAttribute("paymentMethod", paymentMethod);
        model.addAttribute("upiId", "asraopticals@upi");
        model.addAttribute("total", String.format("%.2f", order.getTotalAmount()));
        return "order-success";
    }

    @GetMapping("/track-order")
    public String trackPage() { return "track-order"; }

    @PostMapping("/track-order")
    public String trackOrder(@RequestParam String orderNumber, Model model) {
        Order order = orderRepository.findByOrderNumber(orderNumber.trim());
        model.addAttribute("order", order);
        if (order == null) model.addAttribute("notFound", true);
        return "track-order";
    }

    @GetMapping("/my-orders")
    public String myOrders(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        //List<Order> orders = orderRepository.findByEmail(principal.getName());
        String userEmail = principal.getName();
        User user = userRepository.findByUsername(userEmail).orElse(null);
        if (user != null && user.getEmail() != null) {
            userEmail = user.getEmail();
        }
        List<Order> orders = orderRepository.findByEmail(userEmail);
        model.addAttribute("orders", orders);
        return "my-orders";
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCart(HttpSession session) {
        return (Map<Long, Integer>) session.getAttribute("cart");
    }
}