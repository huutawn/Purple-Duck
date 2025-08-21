package com.tawn.tawnht.repository.jpa;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tawn.tawnht.entity.Order;
import com.tawn.tawnht.entity.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findFirstByUserAndStatusOrderByCreatedAtDesc(User user, String status);

    Page<Order> findAllByUser(User user, Pageable pageable);
    Optional<Order> findByQRCode(String code);
}
