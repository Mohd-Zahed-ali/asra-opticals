package com.asra.asraopticals.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role; // ROLE_ADMIN or ROLE_USER

    @Column(unique = true)
    private String phone;

    private String fullname;

    private Boolean verified = false;

    // OTP fields
    private String otp;
    private Long otpExpiry;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public Boolean getVerified() { return verified; }
    public Boolean isVerified() { return Boolean.TRUE.equals(verified); }
    public void setVerified(Boolean verified) { this.verified = verified; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public Long getOtpExpiry() { return otpExpiry; }
    public void setOtpExpiry(Long otpExpiry) { this.otpExpiry = otpExpiry; }
}