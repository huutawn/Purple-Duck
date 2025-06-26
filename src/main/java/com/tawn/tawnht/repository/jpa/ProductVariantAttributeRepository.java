package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.ProductVariantAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantAttributeRepository extends JpaRepository<ProductVariantAttribute,Long> {
}
