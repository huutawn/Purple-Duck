package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.Seller;
import com.tawn.tawnht.entity.SubOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubOrderRepository extends JpaRepository<SubOrder,Long> {
    @Query("SELECT so FROM SubOrder so WHERE so.seller = :seller AND so.status <> 'init'")
    Page<SubOrder> findAllBySeller(Pageable pageable,@Param("seller") Seller seller);
    void deleteByOrderId(Long orderId);
}
