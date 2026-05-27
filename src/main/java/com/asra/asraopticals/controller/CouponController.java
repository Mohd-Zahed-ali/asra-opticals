package com.asra.asraopticals.controller;

import com.asra.asraopticals.model.Coupon;
import com.asra.asraopticals.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/coupons")
public class CouponController {

    @Autowired private CouponRepository couponRepository;

    @GetMapping
    public String listCoupons(Model model) {
        model.addAttribute("coupons", couponRepository.findAll());
        model.addAttribute("coupon", new Coupon());
        return "admin-coupons";
    }

    @PostMapping("/save")
    public String saveCoupon(@ModelAttribute Coupon coupon, RedirectAttributes attrs) {
        couponRepository.save(coupon);
        attrs.addFlashAttribute("success", "Coupon saved: " + coupon.getCode());
        return "redirect:/admin/coupons";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id) {
        couponRepository.findById(id).ifPresent(c -> {
            c.setActive(!c.isActive());
            couponRepository.save(c);
        });
        return "redirect:/admin/coupons";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        couponRepository.deleteById(id);
        return "redirect:/admin/coupons";
    }
}