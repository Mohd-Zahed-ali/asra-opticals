package com.asra.asraopticals.repository;

import com.asra.asraopticals.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByActiveTrue();
    List<Offer> findByActiveTrueOrderByIdDesc();
}