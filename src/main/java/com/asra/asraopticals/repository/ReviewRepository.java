package com.asra.asraopticals.repository;

import com.asra.asraopticals.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndApprovedTrue(Long productId);
    List<Review> findAllByOrderByCreatedAtDesc();
    boolean existsByProductIdAndEmail(Long productId, String email);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.approved = true")
    Double avgRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.approved = true")
    Long countByProductId(@Param("productId") Long productId);
}