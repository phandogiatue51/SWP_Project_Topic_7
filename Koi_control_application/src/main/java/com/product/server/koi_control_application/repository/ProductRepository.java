package com.product.server.koi_control_application.repository;

import com.product.server.koi_control_application.model.Product;
import com.product.server.koi_control_application.pojo.report.BarChart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
@RepositoryRestResource(exported = false)
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("select p from Product p where p.categoryId = ?1")
    Page<Product> findByCategoryId(int categoryId, Pageable pageable);

    @Query("select p from Product p where p.slug = ?1")
    Optional<Product> findBySlug(String slug);

    @Query("SELECT new com.product.server.koi_control_application.pojo.report.BarChart(p.name, COUNT(oi.productId.id), MAX(oi.createdAt)) " +
            "FROM Product p " +
            "LEFT JOIN OrderItems oi ON p.id = oi.productId.id " +
            "WHERE oi.createdAt >= :startDate AND oi.createdAt <= :endDate " +
            "GROUP BY p.id, p.name " +
            "ORDER BY COUNT(oi.productId.id) DESC")
    List<BarChart> getTopSellingProductsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name%")
    List<Product> findBySlugAndNameContaining(@Param("name") String name);
}
