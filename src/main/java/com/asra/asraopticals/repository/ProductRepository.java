package com.asra.asraopticals.repository;

import com.asra.asraopticals.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();
    List<Product> findByCategoryAndActiveTrue(String category);
    List<Product> findByGenderAndActiveTrue(String gender);
    List<Product> findByShapeAndActiveTrue(String shape);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%',:kw,'%')))")
    List<Product> searchActive(@Param("kw") String keyword);

    List<Product> findByStockLessThanAndActiveTrue(int threshold);
}