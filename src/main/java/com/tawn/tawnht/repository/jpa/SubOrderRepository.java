package com.tawn.tawnht.repository.jpa;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tawn.tawnht.entity.Seller;
import com.tawn.tawnht.entity.SubOrder;

@Repository
public interface SubOrderRepository extends JpaRepository<SubOrder, Long> {
    @Query("SELECT so FROM SubOrder so WHERE so.seller = :seller AND so.status <> 'init'")
    Page<SubOrder> findAllBySeller(Pageable pageable, @Param("seller") Seller seller);

    /**
     * Calculate daily metrics for a seller within a specific date range
     * Returns: [totalRevenue, transactionCount, refundAmount, refundCount]
     */
    @Query("SELECT " + "COALESCE(SUM(CASE WHEN so.status IN ('COMPLETED', 'DELIVERED') THEN "
            + "  (SELECT SUM(oi.price * oi.quantity) FROM OrderItem oi WHERE oi.subOrder = so) "
            + "ELSE 0 END), 0), "
            + "COUNT(CASE WHEN so.status IN ('COMPLETED', 'DELIVERED') THEN 1 END), "
            + "COALESCE(SUM(CASE WHEN so.status = 'RETURNED' THEN "
            + "  (SELECT SUM(oi.price * oi.quantity) FROM OrderItem oi WHERE oi.subOrder = so) "
            + "ELSE 0 END), 0), "
            + "COUNT(CASE WHEN so.status = 'RETURNED' THEN 1 END) "
            + "FROM SubOrder so "
            + "WHERE so.seller.id = :sellerId "
            + "AND so.createdAt BETWEEN :startDate AND :endDate")
    List<Object[]> findDailyMetricsBySeller(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    void deleteByOrderId(Long orderId);
}
