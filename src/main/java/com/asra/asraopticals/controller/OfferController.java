package com.asra.asraopticals.controller;

import com.asra.asraopticals.model.Offer;
import com.asra.asraopticals.repository.OfferRepository;
import com.asra.asraopticals.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OfferController {

    @Autowired private OfferRepository offerRepository;
    @Autowired private ProductRepository productRepository;

    // ── Public offers page ─────────────────────────────────────────────────

    @GetMapping("/offers")
    public String offersPage(Model model) {
        model.addAttribute("offers", offerRepository.findByActiveTrueOrderByIdDesc());
        return "offers";
    }

    // ── Admin ──────────────────────────────────────────────────────────────

    @GetMapping("/admin/offers")
    public String adminOffers(Model model) {
        model.addAttribute("offers", offerRepository.findAll());
        model.addAttribute("products", productRepository.findByActiveTrue());
        model.addAttribute("offer", new Offer());
        return "admin-offers";
    }

    @PostMapping("/admin/offers/save")
    public String saveOffer(@ModelAttribute Offer offer,
                            @RequestParam(required = false) Long productId,
                            RedirectAttributes attrs) {
        if (productId != null) {
            productRepository.findById(productId).ifPresent(offer::setProduct);
        }
        offerRepository.save(offer);
        attrs.addFlashAttribute("success", "Offer saved: " + offer.getTitle());
        return "redirect:/admin/offers";
    }

    @PostMapping("/admin/offers/toggle/{id}")
    public String toggle(@PathVariable Long id) {
        offerRepository.findById(id).ifPresent(o -> {
            o.setActive(!o.isActive());
            offerRepository.save(o);
        });
        return "redirect:/admin/offers";
    }

    @PostMapping("/admin/offers/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes attrs) {
        offerRepository.deleteById(id);
        attrs.addFlashAttribute("success", "Offer deleted.");
        return "redirect:/admin/offers";
    }
}