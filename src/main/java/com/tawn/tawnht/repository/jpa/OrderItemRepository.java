package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.Category;
import com.tawn.tawnht.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
    void deleteBySubOrderId(Long subOrderId);
}
