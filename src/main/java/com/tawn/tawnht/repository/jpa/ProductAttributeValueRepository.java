package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.ProductAttribute;
import com.tawn.tawnht.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue,Long> {
    Optional<ProductAttributeValue> findByProductAttributeAndValue(ProductAttribute productAttribute,String value);
}
