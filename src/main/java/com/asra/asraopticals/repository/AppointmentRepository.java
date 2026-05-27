package com.asra.asraopticals.repository;

import com.asra.asraopticals.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByEmail(String email);
    List<Appointment> findByStatus(String status);
    List<Appointment> findAllByOrderByAppointmentDateDescAppointmentTimeDesc();
}