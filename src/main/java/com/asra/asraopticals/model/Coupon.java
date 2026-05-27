package com.asra.asraopticals.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // e.g. ASRA10

    private String type; // PERCENT or FIXED
    private double discountValue; // 10 = 10% or ₹10 off
    private double minOrderAmount; // minimum order to apply
    private int usageLimit;
    private int usedCount = 0;
    private boolean active = true;

    private java.time.LocalDateTime expiryDate;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code.toUpperCase().trim(); }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getDiscountValue() { return discountValue; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }

    public double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(double minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }

    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public java.time.LocalDateTime getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(java.time.LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}
	// Helper
    public boolean isValid() {
        if (!active) return false;
        if (usageLimit > 0 && usedCount >= usageLimit) return false;
        if (expiryDate != null && java.time.LocalDateTime.now().isAfter(expiryDate)) return false;
        return true;
    }

    public double calculateDiscount(double orderTotal) {
        if ("PERCENT".equals(type)) {
            return Math.round(orderTotal * discountValue / 100.0 * 100.0) / 100.0;
        }
        return Math.min(discountValue, orderTotal);
    }
}