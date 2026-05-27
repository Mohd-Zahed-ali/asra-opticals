package com.asra.asraopticals.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String brand;
    private String category;  // Glasses, Sunglasses, Contact Lenses, Electronics
    private String gender;    // Men, Women, Kids, Unisex
    private String shape;     // Square, Rectangle, Round, Aviator
    private Double price;
    private String imageName; // primary image filename
    private String images;    // comma-separated list of filenames
    private int stock = 0;
    private boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getShape() { return shape; }
    public void setShape(String shape) { this.shape = shape; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { this.orderItems = orderItems; }
}