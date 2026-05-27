package com.asra.asraopticals.controller;

import com.asra.asraopticals.model.Order;
import com.asra.asraopticals.repository.OrderRepository;
import com.asra.asraopticals.repository.ProductRepository;
import com.asra.asraopticals.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminOrderController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private EmailService emailService;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        long totalProducts = productRepository.count();
        long totalOrders   = orderRepository.count();
        long pending       = orderRepository.findByStatus("PENDING").size();
        double revenue     = orderRepository.findAll().stream()
                             .mapToDouble(Order::getTotalAmount).sum();

        model.addAttribute("products", totalProducts);
        model.addAttribute("orders", totalOrders);
        model.addAttribute("revenue", String.format("%.2f", revenue));
        model.addAttribute("pending", pending);
        model.addAttribute("recentOrders", orderRepository.findTop10ByOrderByOrderDateDesc());
        return "admin-dashboard";
    }

    @GetMapping("/admin/orders")
    public String viewOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        return "admin-orders";
    }

    @GetMapping("/admin/order/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        orderRepository.findById(id).ifPresent(o -> model.addAttribute("order", o));
        return "admin-order-detail";
    }

    @PostMapping("/admin/update-status")
    public String updateStatus(@RequestParam Long orderId,
                               @RequestParam String status,
                               RedirectAttributes attrs) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            orderRepository.save(order);
            // Send status email to customer (Point 10)
            emailService.sendStatusUpdate(order);
        });
        attrs.addFlashAttribute("success", "Status updated to " + status);
        return "redirect:/admin/orders";
    }
}