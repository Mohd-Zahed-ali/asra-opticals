package com.asra.asraopticals.controller;

import com.asra.asraopticals.model.User;
import com.asra.asraopticals.repository.UserRepository;
import com.asra.asraopticals.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Random;

@Controller
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;

    // ── REGISTER ───────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam(required = false) String email,
                               @RequestParam String phone,
                               @RequestParam String fullName,
                               @RequestParam String password,
                               @RequestParam(defaultValue = "+91") String countryCode,
                               HttpSession session,
                               Model model) {

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already taken");
            return "register";
        }
        if (userRepository.findByPhone(phone).isPresent()) {
            model.addAttribute("error", "Phone number already registered");
            return "register";
        }

        // Email is required for OTP
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email is required for verification");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email.trim().toLowerCase());
        user.setPhone(countryCode + phone);
        user.setFullname(fullName);
        user.setPassword(password); // raw — will be encoded after OTP
        user.setRole("ROLE_USER");

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(1000000));

        session.setAttribute("tempUser", user);
        session.setAttribute("otp", otp);
        session.setAttribute("otpExpiry", System.currentTimeMillis() + 5 * 60 * 1000L);

        // Send OTP to EMAIL (not username!)
        emailService.sendOtp(user.getEmail(), otp);

        return "redirect:/verify?email=" + user.getEmail();
    }

    // ── VERIFY OTP ─────────────────────────────────────────────────────────

    @GetMapping("/verify")
    public String verifyPage(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "verify";
    }

    @PostMapping("/verify")
    public String verifyOtp(@RequestParam String otp,
                            HttpSession session,
                            RedirectAttributes attrs,Model model) {

        String sessionOtp = (String) session.getAttribute("otp");
        Long expiry = (Long) session.getAttribute("otpExpiry");
        User user = (User) session.getAttribute("tempUser");

        if (user == null) return "redirect:/register";

        if (!otp.equals(sessionOtp) || System.currentTimeMillis() > expiry) {
            session.invalidate();
            attrs.addFlashAttribute("error", "Invalid or expired OTP. Please register again.");
            return "redirect:/register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
     // Check for duplicate phone before saving
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            boolean phoneExists = userRepository.findByPhone(user.getPhone())
                .filter(existing -> !existing.getId().equals(user.getId()))
                .isPresent();
            if (phoneExists) {
                model.addAttribute("error", "This phone number is already registered. Please use a different number or login.");
                return "verify";
            }
        }
        if (user.getEmail() !=null && !user.getEmail().isBlank()) {
        	 boolean emailExists = userRepository.findByEmail(user.getEmail())
        			 .filter(existing -> !existing.getId().equals(user.getId()))
        			 .isPresent();
        	 if(emailExists) {
        		 model.addAttribute("error", "This Email is already registered. Please use a different Email or login.");
        		 return "verify";
        	 }
        }
        if (user.getUsername() !=null && !user.getUsername().isBlank()) {
       	 boolean userExists = userRepository.findByUsername(user.getUsername())
       			 .filter(existing -> !existing.getId().equals(user.getId()))
       			 .isPresent();
       	 if(userExists) {
       		 model.addAttribute("error", "This Username is already registered. Please use a different username to login.");
       		return "verify";
       	 }
       }
        user.setVerified(true);
        userRepository.save(user);

        session.invalidate();
        attrs.addFlashAttribute("success", "Account verified! Please login.");
        return "redirect:/login?success";
    }

    // ── PROFILE ────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String fullName,
                                Principal principal,
                                RedirectAttributes attrs) {

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        user.setEmail(email);
        user.setPhone(phone);
        user.setFullname(fullName);
        userRepository.save(user);

        attrs.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }

    // ── CHANGE PASSWORD ────────────────────────────────────────────────────

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes attrs) {

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            attrs.addFlashAttribute("passError", "Current password is incorrect");
            return "redirect:/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            attrs.addFlashAttribute("passError", "New passwords do not match");
            return "redirect:/profile";
        }

        if (newPassword.length() < 6) {
            attrs.addFlashAttribute("passError", "Password must be at least 6 characters");
            return "redirect:/profile";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        attrs.addFlashAttribute("passSuccess", "Password changed successfully!");
        return "redirect:/profile";
    }

    // ── FORGOT PASSWORD ────────────────────────────────────────────────────

    @GetMapping("/forgot-password")
    public String forgotPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendResetOtp(@RequestParam String email, Model model) {

        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);

        if (user == null) {
            model.addAttribute("error", "No account found with this email");
            return "forgot-password";
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));
        user.setOtp(otp);
        user.setOtpExpiry(System.currentTimeMillis() + 5 * 60 * 1000L);
        userRepository.save(user);

        emailService.sendOtp(email, otp);
        model.addAttribute("email", email);
        model.addAttribute("otpSent", true);
        return "forgot-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String otp,
                                @RequestParam String newPassword,
                                RedirectAttributes attrs) {

        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
        if (user == null) return "redirect:/forgot-password";

        if (!otp.equals(user.getOtp()) ||
                System.currentTimeMillis() > user.getOtpExpiry()) {
            attrs.addFlashAttribute("error", "Invalid or expired OTP");
            return "redirect:/forgot-password";
        }

        if (newPassword.length() < 6) {
            attrs.addFlashAttribute("error", "Password must be at least 6 characters");
            return "redirect:/forgot-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        attrs.addFlashAttribute("success", "Password reset successfully! Please login.");
        return "redirect:/login";
    }
}