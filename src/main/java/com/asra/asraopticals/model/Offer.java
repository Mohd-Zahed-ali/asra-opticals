package com.asra.asraopticals.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;          // e.g. "Buy 1 Get 1 Free"
    private String offerType;      // DISCOUNT, BUY1GET1, BUY2GET1, FLAT_OFF
    private String description;    // Full description shown to customer
    private String badgeText;      // Short badge e.g. "50% OFF", "B1G1"
    private String badgeColor;     // e.g. "#e74c3c"
    private Double discountPercent;// For DISCOUNT type
    private boolean active = true;

    private java.time.LocalDateTime validUntil;

    // Link to product
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOfferType() { return offerType; }
    public void setOfferType(String offerType) { this.offerType = offerType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBadgeText() { return badgeText; }
    public void setBadgeText(String badgeText) { this.badgeText = badgeText; }

    public String getBadgeColor() { return badgeColor; }
    public void setBadgeColor(String badgeColor) { this.badgeColor = badgeColor; }

    public Double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Double discountPercent) { this.discountPercent = discountPercent; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    

    public java.time.LocalDateTime getValidUntil() {
		return validUntil;
	}
	public void setValidUntil(java.time.LocalDateTime validUntil) {
		this.validUntil = validUntil;
	}
	public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    // Helper - is offer still valid?
    public boolean isValid() {
        return active && (validUntil == null || java.time.LocalDateTime.now().isBefore(validUntil));
    }

    // Display price after discount
    public Double getDiscountedPrice() {
        if (product == null || product.getPrice() == null) return null;
        if ("DISCOUNT".equals(offerType) && discountPercent != null) {
            return Math.round(product.getPrice() * (1 - discountPercent / 100) * 100.0) / 100.0;
        }
        return product.getPrice();
    }
}