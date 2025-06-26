package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.ProductAttribute;
import com.tawn.tawnht.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute,Long> {
    Optional<ProductAttribute> findByName(String name);
}
