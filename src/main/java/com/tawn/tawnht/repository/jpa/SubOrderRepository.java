package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.Seller;
import com.tawn.tawnht.entity.SubOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubOrderRepository extends JpaRepository<SubOrder,Long> {
    @Query("SELECT o FROM Order where o.status <> 'init'")
    Page<SubOrder> findAllBySeller(Pageable pageable, Seller seller);
    void deleteByOrderId(Long orderId);
}
