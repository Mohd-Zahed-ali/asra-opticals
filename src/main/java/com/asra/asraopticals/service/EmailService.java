package com.asra.asraopticals.service;

import com.asra.asraopticals.model.*;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {
	@Value("${app.base-url:baseUrl}")
	private String baseUrl;

    @Autowired private JavaMailSender mailSender;

    @Value("${spring.mail.username}") private String storeEmail;
    @Value("${store.name:Asra Optical Palace}") private String storeName;
    @Value("${store.upi:asraopticals@upi}") private String storeUpi;
    @Value("${store.whatsapp:919876543210}") private String storeWhatsapp;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String header(String title) {
        return "<div style='font-family:Georgia,serif;max-width:620px;margin:auto;border:1px solid #e0d5c5;border-radius:6px;overflow:hidden'>"
             + "<div style='background:#1a1a1a;padding:24px 30px;text-align:center'>"
             + "<h1 style='color:#c9a96e;margin:0;font-size:22px;letter-spacing:2px'>ASRA OPTICAL PALACE</h1>"
             + "<p style='color:#aaa;margin:6px 0 0;font-size:13px'>" + title + "</p></div>"
             + "<div style='padding:28px 30px'>";
    }

    private String footer() {
        return "<hr style='border:none;border-top:1px solid #e0d5c5;margin:24px 0'>"
             + "<p style='color:#aaa;font-size:12px;text-align:center'>"
             + "📍 Hyderabad, Telangana &nbsp;|&nbsp; "
             + "<a href='https://wa.me/" + storeWhatsapp + "' style='color:#c9a96e'>💬 WhatsApp</a>"
             + "</p></div></div>";
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true);
            h.setTo(to);
            h.setSubject(subject);
            h.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            System.err.println("Email failed to " + to + ": " + e.getMessage());
        }
    }

    // ── OTP ───────────────────────────────────────────────────────────────────

    @Async
    public void sendOtp(String toEmail, String otp) {
        String body = header("Email Verification")
            + "<p style='font-size:15px'>Your OTP for account verification:</p>"
            + "<div style='background:#1a1a1a;color:#c9a96e;font-size:38px;font-weight:bold;"
            + "text-align:center;padding:22px;letter-spacing:14px;border-radius:4px;margin:20px 0'>" + otp + "</div>"
            + "<p style='color:#666;font-size:13px'>Expires in <b>5 minutes</b>. If you didn't request this, ignore this email.</p>"
            + footer();
        send(toEmail, "🔐 Your OTP — " + storeName, body);
    }

    // ── ORDER CONFIRMATION ────────────────────────────────────────────────────

    @Async
    public void sendOrderConfirmation(String toEmail, Order order) {
        StringBuilder items = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            items.append("<tr><td style='padding:8px;border-top:1px solid #f0ece4'>")
                 .append(item.getProduct().getName()).append("</td>")
                 .append("<td style='padding:8px;border-top:1px solid #f0ece4;text-align:center'>")
                 .append(item.getQuantity()).append("</td>")
                 .append("<td style='padding:8px;border-top:1px solid #f0ece4;text-align:right'>₹")
                 .append(String.format("%.2f", item.getPrice() * item.getQuantity())).append("</td></tr>");
        }

        String upiSection = "";
        if ("UPI".equals(order.getPaymentMethod())) {
            upiSection = "<div style='background:#f9f6f0;border:1px solid #e0d5c5;border-radius:4px;padding:16px;margin:16px 0'>"
                + "<p style='margin:0 0 8px;font-weight:bold;font-size:14px'>💳 UPI Payment Details</p>"
                + "<p style='margin:0;font-size:14px'>UPI ID: <b style='color:#1a1a1a'>" + storeUpi + "</b></p>"
                + "<p style='margin:6px 0 0;font-size:13px;color:#666'>Please send ₹" + String.format("%.2f", order.getTotalAmount())
                + " and reply with your UTR/transaction reference.</p></div>";
        }

        String discountRow = order.getDiscountAmount() > 0
            ? "<tr><td style='padding:6px'>Discount (" + order.getCouponCode() + ")</td>"
              + "<td style='padding:6px;text-align:right;color:green'>-₹" + String.format("%.2f", order.getDiscountAmount()) + "</td></tr>"
            : "";

        String body = header("Order Confirmed ✅")
            + "<p>Hello <b>" + order.getCustomerName() + "</b>,</p>"
            + "<p>Thank you for your order! Here are your details:</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:12px 0;font-size:13px'>"
            + "<tr><td style='padding:6px;background:#f9f6f0;font-weight:bold'>Order ID</td><td style='padding:6px'>" + order.getOrderNumber() + "</td></tr>"
            + "<tr><td style='padding:6px;font-weight:bold'>Phone</td><td style='padding:6px'>" + order.getPhone() + "</td></tr>"
            + "<tr><td style='padding:6px;background:#f9f6f0;font-weight:bold'>Address</td><td style='padding:6px'>" + order.getAddress() + "</td></tr>"
            + "<tr><td style='padding:6px;font-weight:bold'>Payment</td><td style='padding:6px'>" + order.getPaymentMethod() + "</td></tr>"
            + "</table>"
            + "<h3 style='border-bottom:1px solid #e0d5c5;padding-bottom:8px'>Items</h3>"
            + "<table style='width:100%;border-collapse:collapse;font-size:13px'>"
            + "<tr style='background:#f9f6f0'><th style='padding:8px;text-align:left'>Item</th><th style='padding:8px'>Qty</th><th style='padding:8px;text-align:right'>Total</th></tr>"
            + items
            + "</table>"
            + "<table style='width:100%;margin-top:8px;font-size:13px'>"
            + discountRow
            + "<tr><td style='padding:6px;font-weight:bold;font-size:16px'>Total</td>"
            + "<td style='padding:6px;text-align:right;font-weight:bold;font-size:16px'>₹" + String.format("%.2f", order.getTotalAmount()) + "</td></tr>"
            + "</table>"
            + upiSection
            + "<p style='margin-top:18px;color:#666;font-size:13px'>We'll contact you soon to confirm. Track your order at <a href='baseUrl/track-order' style='color:#c9a96e'>Track Order</a></p>"
            + footer();
        send(toEmail, "✅ Order Confirmed — " + order.getOrderNumber(), body);
    }

    // ── APPOINTMENT ───────────────────────────────────────────────────────────

    @Async
    public void sendAppointmentNotification(Appointment appt) {
        String body = header("New Appointment 📅")
            + "<table style='width:100%;border-collapse:collapse;font-size:13px'>"
            + "<tr><td style='padding:8px;background:#f9f6f0;font-weight:bold'>Name</td><td style='padding:8px'>" + appt.getCustomerName() + "</td></tr>"
            + "<tr><td style='padding:8px;font-weight:bold'>Email</td><td style='padding:8px'>" + appt.getEmail() + "</td></tr>"
            + "<tr><td style='padding:8px;background:#f9f6f0;font-weight:bold'>Phone</td><td style='padding:8px'>" + appt.getPhone() + "</td></tr>"
            + "<tr><td style='padding:8px;font-weight:bold'>Date</td><td style='padding:8px'>" + appt.getAppointmentDate() + "</td></tr>"
            + "<tr><td style='padding:8px;background:#f9f6f0;font-weight:bold'>Time</td><td style='padding:8px'>" + appt.getAppointmentTime() + "</td></tr>"
            + "<tr><td style='padding:8px;font-weight:bold'>Purpose</td><td style='padding:8px'>" + appt.getPurpose() + "</td></tr>"
            + (appt.getNotes() != null && !appt.getNotes().isEmpty()
                ? "<tr><td style='padding:8px;background:#f9f6f0;font-weight:bold'>Notes</td><td style='padding:8px'>" + appt.getNotes() + "</td></tr>" : "")
            + "</table>" + footer();
        send(storeEmail, "📅 New Appointment — " + appt.getCustomerName(), body);

        if (appt.getEmail() != null && !appt.getEmail().isEmpty()) {
            String custBody = header("Appointment Received")
                + "<p>Hello <b>" + appt.getCustomerName() + "</b>,</p>"
                + "<p>Your appointment request for <b>" + appt.getAppointmentDate() + " at " + appt.getAppointmentTime()
                + "</b> has been received.</p>"
                + "<p>Purpose: <b>" + appt.getPurpose() + "</b></p>"
                + "<p>We'll confirm shortly via WhatsApp/Email.</p>" + footer();
            send(appt.getEmail(), "📅 Appointment Received — " + storeName, custBody);
        }
    }

    // ── LOW STOCK ALERT (Point 9) ─────────────────────────────────────────────

    @Async
    public void sendLowStockAlert(List<com.asra.asraopticals.model.Product> products) {
        if (products.isEmpty()) return;
        StringBuilder rows = new StringBuilder();
        for (var p : products) {
            rows.append("<tr><td style='padding:8px;border-top:1px solid #f0ece4'>").append(p.getName()).append("</td>")
                .append("<td style='padding:8px;border-top:1px solid #f0ece4;text-align:center;color:")
                .append(p.getStock() == 0 ? "#e74c3c" : "#e67e22").append(";font-weight:bold'>")
                .append(p.getStock() == 0 ? "OUT OF STOCK" : p.getStock() + " left").append("</td>")
                .append("<td style='padding:8px;border-top:1px solid #f0ece4'>").append(p.getCategory()).append("</td></tr>");
        }
        String body = header("⚠️ Low Stock Alert")
            + "<p>The following products need restocking:</p>"
            + "<table style='width:100%;border-collapse:collapse;font-size:13px'>"
            + "<tr style='background:#f9f6f0'><th style='padding:8px;text-align:left'>Product</th><th style='padding:8px'>Stock</th><th style='padding:8px;text-align:left'>Category</th></tr>"
            + rows + "</table>"
            + "<p style='margin-top:16px'><a href='baseUrl/admin/products' style='background:#1a1a1a;color:#c9a96e;padding:10px 20px;text-decoration:none;border-radius:3px'>Manage Products →</a></p>"
            + footer();
        send(storeEmail, "⚠️ Low Stock Alert — " + products.size() + " products need attention", body);
    }

    // ── ORDER STATUS UPDATE ───────────────────────────────────────────────────

    @Async
    public void sendStatusUpdate(Order order) {
    	if (order.getEmail() == null || order.getEmail().equals("guest") || !order.getEmail().contains("@")) return;
        String emoji = switch (order.getStatus()) {
            case "CONFIRMED" -> "✅";
            case "READY_AT_STORE" -> "🏪";
            case "SHIPPED" -> "🚚";
            case "COMPLETED" -> "🎉";
            case "CANCELLED" -> "❌";
            default -> "📋";
        };
        String body = header(emoji + " Order Status Updated")
            + "<p>Hello <b>" + order.getCustomerName() + "</b>,</p>"
            + "<p>Your order <b>" + order.getOrderNumber() + "</b> status has been updated to:</p>"
            + "<div style='background:#1a1a1a;color:#c9a96e;font-size:22px;font-weight:bold;"
            + "text-align:center;padding:18px;border-radius:4px;margin:16px 0;letter-spacing:2px'>"
            + emoji + " " + order.getStatus() + "</div>"
            + (order.getStatus().equals("READY_AT_STORE")
                ? "<p style='color:#155724;background:#d4edda;padding:12px;border-radius:4px'>Your order is ready for pickup at our store!</p>" : "")
            + "<p><a href='baseUrl/track-order' style='background:#1a1a1a;color:#c9a96e;padding:10px 20px;text-decoration:none;border-radius:3px'>Track Order →</a></p>"
            + footer();
        send(order.getEmail(), emoji + " Order Update — " + order.getOrderNumber(), body);
    }
}
