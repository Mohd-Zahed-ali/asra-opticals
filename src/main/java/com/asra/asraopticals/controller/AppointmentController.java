package com.asra.asraopticals.controller;

import com.asra.asraopticals.model.Appointment;
import com.asra.asraopticals.repository.AppointmentRepository;
import com.asra.asraopticals.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/book-appointment")
    public String bookPage(Model model) {
        model.addAttribute("appointment", new Appointment());
        return "book-appointment";
    }

    @PostMapping("/book-appointment")
    public String bookAppointment(@ModelAttribute Appointment appointment,
                                  Model model) {
        appointment.setStatus("PENDING");
        appointmentRepository.save(appointment);

        // Notify store owner
        emailService.sendAppointmentNotification(appointment);

        model.addAttribute("success",
            "Appointment booked! We'll confirm via WhatsApp/Email soon.");
        model.addAttribute("appointment", new Appointment());
        return "book-appointment";
    }

    // ── Admin ──────────────────────────────────────────────────────────────

    @GetMapping("/admin/appointments")
    public String adminAppointments(Model model) {
        model.addAttribute("appointments",
            appointmentRepository.findAllByOrderByAppointmentDateDescAppointmentTimeDesc());
        return "admin-appointments";
    }

    @PostMapping("/admin/appointments/update-status")
    public String updateStatus(@RequestParam Long id,
                               @RequestParam String status) {
        appointmentRepository.findById(id).ifPresent(a -> {
            a.setStatus(status);
            appointmentRepository.save(a);
        });
        return "redirect:/admin/appointments";
    }
}