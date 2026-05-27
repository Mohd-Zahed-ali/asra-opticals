package com.asra.asraopticals.repository;

import com.asra.asraopticals.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByOrderNumber(String orderNumber);
    List<Order> findByEmail(String email);
    List<Order> findByStatus(String status);
    List<Order> findTop10ByOrderByOrderDateDesc();
}