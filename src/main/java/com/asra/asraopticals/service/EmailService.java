package com.asra.asraopticals.service;

import com.asra.asraopticals.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class EmailService {

    @Value("${brevo.api.key:}") private String brevoApiKey;
    @Value("${store.name:Asra Optical Palace}") private String storeName;
    @Value("${store.upi:asraopticals@upi}") private String storeUpi;
    @Value("${store.whatsapp:919876543210}") private String storeWhatsapp;
    @Value("${spring.mail.username:zahedali00830@gmail.com}") private String storeEmail;
    @Value("${app.base-url:https://asra-opticals.onrender.com}") private String baseUrl;

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
            String body = "{"
                + "\"sender\":{\"name\":\"" + storeName + "\",\"email\":\"zahedali00830@gmail.com\"},"
                + "\"to\":[{\"email\":\"" + to + "\"}],"
                + "\"subject\":\"" + subject.replace("\"", "'") + "\","
                + "\"htmlContent\":\"" + html.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "").replace("\r", "") + "\""
                + "}";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("accept", "application/json")
                .header("api-key", brevoApiKey)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201) {
                System.out.println("Email sent to " + to);
            } else {
                System.err.println("Email failed to " + to + ": " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Email failed to " + to + ": " + e.getMessage());
        }
    }

    @Async
    public void sendOtp(String toEmail, String otp) {
        String body = header("Email Verification")
            + "<p style='font-size:15px'>Your OTP for account verification:</p>"
            + "<div style='background:#1a1a1a;color:#c9a96e;font-size:38px;font-weight:bold;"
            + "text-align:center;padding:22px;letter-spacing:14px;border-radius:4px;margin:20px 0'>" + otp + "</div>"
            + "<p style='color:#666;font-size:13px'>Expires in <b>5 minutes</b>. If you did not request this, ignore this email.</p>"
            + footer();
        send(toEmail, "Your OTP - " + storeName, body);
    }

    @Async
    public void sendOrderConfirmation(String toEmail, Order order) {
        if (toEmail == null || toEmail.equals("guest") || !toEmail.contains("@")) return;
        StringBuilder items = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            items.append("<tr><td style='padding:8px;border-top:1px solid #f0ece4'>")
                 .append(item.getProduct().getName()).append("</td>")
                 .append("<td style='padding:8px;border-top:1px solid #f0ece4;text-align:center'>")
                 .append(item.getQuantity()).append("</td>")
                 .append("<td style='padding:8px;border-top:1px solid #f0ece4;text-align:right'>Rs.")
                 .append(String.format("%.2f", item.getPrice() * item.getQuantity())).append("</td></tr>");
        }
        String body = header("Order Confirmed")
            + "<p>Hello <b>" + order.getCustomerName() + "</b>,</p>"
            + "<p>Thank you for your order!</p>"
            + "<table style='width:100%;border-collapse:collapse;font-size:13px'>"
            + "<tr><td style='padding:6px;background:#f9f6f0;font-weight:bold'>Order ID</td><td style='padding:6px'>" + order.getOrderNumber() + "</td></tr>"
            + "<tr><td style='padding:6px;font-weight:bold'>Payment</td><td style='padding:6px'>" + order.getPaymentMethod() + "</td></tr>"
            + "</table>"
            + "<h3 style='border-bottom:1px solid #e0d5c5;padding-bottom:8px;margin-top:16px'>Items</h3>"
            + "<table style='width:100%;border-collapse:collapse;font-size:13px'>"
            + "<tr style='background:#f9f6f0'><th style='padding:8px;text-align:left'>Item</th><th>Qty</th><th style='text-align:right'>Total</th></tr>"
            + items
            + "</table>"
            + "<p style='font-size:16px;font-weight:bold;margin-top:12px'>Total: Rs." + String.format("%.2f", order.getTotalAmount()) + "</p>"
            + footer();
        send(toEmail, "Order Confirmed - " + order.getOrderNumber(), body);
    }

    @Async
    public void sendAppointmentNotification(Appointment appt) {
        String body = header("New Appointment")
            + "<p>Name: <b>" + appt.getCustomerName() + "</b></p>"
            + "<p>Date: <b>" + appt.getAppointmentDate() + " at " + appt.getAppointmentTime() + "</b></p>"
            + "<p>Purpose: <b>" + appt.getPurpose() + "</b></p>"
            + footer();
        send(storeEmail, "New Appointment - " + appt.getCustomerName(), body);

        if (appt.getEmail() != null && !appt.getEmail().isEmpty()) {
            String custBody = header("Appointment Received")
                + "<p>Hello <b>" + appt.getCustomerName() + "</b>,</p>"
                + "<p>Your appointment for <b>" + appt.getAppointmentDate() + " at " + appt.getAppointmentTime() + "</b> has been received.</p>"
                + "<p>We will confirm shortly.</p>" + footer();
            send(appt.getEmail(), "Appointment Received - " + storeName, custBody);
        }
    }

    @Async
    public void sendLowStockAlert(List<Product> products) {
        if (products.isEmpty()) return;
        StringBuilder rows = new StringBuilder();
        for (var p : products) {
            rows.append("<tr><td style='padding:8px'>").append(p.getName()).append("</td>")
                .append("<td style='padding:8px;color:").append(p.getStock() == 0 ? "#e74c3c" : "#e67e22").append(";font-weight:bold'>")
                .append(p.getStock() == 0 ? "OUT OF STOCK" : p.getStock() + " left").append("</td></tr>");
        }
        String body = header("Low Stock Alert")
            + "<p>These products need restocking:</p>"
            + "<table style='width:100%;border-collapse:collapse;font-size:13px'>" + rows + "</table>"
            + footer();
        send(storeEmail, "Low Stock Alert - " + products.size() + " products", body);
    }

    @Async
    public void sendStatusUpdate(Order order) {
        if (order.getEmail() == null || order.getEmail().equals("guest") || !order.getEmail().contains("@")) return;
        String body = header("Order Status Updated")
            + "<p>Hello <b>" + order.getCustomerName() + "</b>,</p>"
            + "<p>Your order <b>" + order.getOrderNumber() + "</b> status: <b>" + order.getStatus() + "</b></p>"
            + "<p><a href='" + baseUrl + "/track-order' style='background:#1a1a1a;color:#c9a96e;padding:10px 20px;text-decoration:none;border-radius:3px'>Track Order</a></p>"
            + footer();
        send(order.getEmail(), "Order Update - " + order.getOrderNumber(), body);
    }
}