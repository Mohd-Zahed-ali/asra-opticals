package com.asra.asraopticals.service;

import com.asra.asraopticals.model.Coupon;
import com.asra.asraopticals.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    public record CouponResult(boolean valid, String message, double discount, Coupon coupon) {}

    public CouponResult validate(String code, double orderTotal) {
        Optional<Coupon> opt = couponRepository.findByCodeIgnoreCase(code);
        if (opt.isEmpty()) return new CouponResult(false, "Invalid coupon code", 0, null);

        Coupon c = opt.get();
        if (!c.isValid()) return new CouponResult(false, "Coupon expired or no longer valid", 0, null);
        if (orderTotal < c.getMinOrderAmount())
            return new CouponResult(false,
                "Minimum order ₹" + (int) c.getMinOrderAmount() + " required for this coupon", 0, null);

        double discount = c.calculateDiscount(orderTotal);
        String msg = c.getType().equals("PERCENT")
                ? (int) c.getDiscountValue() + "% off applied! You save ₹" + String.format("%.2f", discount)
                : "₹" + (int) c.getDiscountValue() + " off applied!";
        return new CouponResult(true, msg, discount, c);
    }

    public void incrementUsage(String code) {
        couponRepository.findByCodeIgnoreCase(code).ifPresent(c -> {
            c.setUsedCount(c.getUsedCount() + 1);
            couponRepository.save(c);
        });
    }
}