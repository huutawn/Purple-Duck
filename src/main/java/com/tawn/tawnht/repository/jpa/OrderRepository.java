package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.Category;
import com.tawn.tawnht.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
}
