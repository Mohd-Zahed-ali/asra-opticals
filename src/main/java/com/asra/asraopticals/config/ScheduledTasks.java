package com.asra.asraopticals.config;

import com.asra.asraopticals.model.Product;
import com.asra.asraopticals.repository.ProductRepository;
import com.asra.asraopticals.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledTasks {

    @Autowired private ProductRepository productRepository;
    @Autowired private EmailService emailService;

    @Value("${store.low-stock-threshold:5}")
    private int threshold;

    // Runs every day at 9:00 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void checkLowStock() {
        List<Product> lowStock = productRepository.findByStockLessThanAndActiveTrue(threshold + 1);
        if (!lowStock.isEmpty()) {
            emailService.sendLowStockAlert(lowStock);
            System.out.println("⚠️ Low stock alert sent for " + lowStock.size() + " products");
        }
    }
}