package com.product.server.koi_control_application.repository;

import com.product.server.koi_control_application.model.Orders;
import com.product.server.koi_control_application.pojo.report.BarChart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
@RepositoryRestResource(exported = false)
public interface OrderRepository extends JpaRepository<Orders, Integer> {
    @Query("select o from Orders o where o.userId = ?1")
    Page<Orders> findOrdersByUserId(int userId, Pageable pageable);

    @Query("select o from Orders o where o.userId = ?1 and o.id = ?2")
    Optional<Orders> findByUserIdAndId(int userId, int id);


    @Query("select o from Orders o where o.userId = ?1")
    List<Orders> findByUserId(int userId);

    @Modifying
    @Query("UPDATE Orders o SET o.status = :newStatus WHERE o.status = :currentStatus AND o.updatedAt < :cutoffDate")
    int updateOrderStatusForOldOrders(@Param("newStatus") String newStatus,
                                      @Param("currentStatus") String currentStatus,
                                      @Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("UPDATE Orders o SET o.status = :newStatus WHERE o.status = :currentStatus")
    int updateSimulatorOrder(@Param("newStatus") String newStatus,
                             @Param("currentStatus") String currentStatus);

    @Query("SELECT new com.product.server.koi_control_application.pojo.report.BarChart(o.status, COUNT(o.id), MAX(o.createdAt)) " +
            "FROM Orders o " +
            "WHERE o.status IN ('COMPLETED', 'CANCELLED') " +
            "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
            "GROUP BY o.status " +
            "ORDER BY COUNT(o.id) DESC")
    List<BarChart> getOrdersStatusByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new com.product.server.koi_control_application.pojo.report.BarChart(DATE(o.createdAt), SUM(o.totalAmount), MAX(o.createdAt)) " +
            "FROM Orders o " +
            "WHERE o.status = 'COMPLETED' " +
            "AND o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(o.createdAt) " +
            "ORDER BY DATE(o.createdAt)")
    List<BarChart> getTotalSalesByDate(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Orders o LEFT JOIN OrderItems oi ON o.id = oi.order.id WHERE oi.productId.id = :productId and o.userId =:userId and o.status = 'COMPLETED'")
    List<Orders> findOrdersByProductId(
            @Param("productId") int productId,
            @Param("userId") int userId
    );
}
