package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.Cart;
import com.tawn.tawnht.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,Long> {
    List<CartItem> findAllByCartId(Long cartId);
}
