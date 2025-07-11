package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.ProductVariantAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantAttributeRepository extends JpaRepository<ProductVariantAttribute,Long> {
    List<ProductVariantAttribute> findByProductAttributeValueIdIn(List<Long> attributeValueIds);
    @Modifying
    @Query("DELETE FROM ProductVariantAttribute")
    void deleteAllVariantAttributes();
}
