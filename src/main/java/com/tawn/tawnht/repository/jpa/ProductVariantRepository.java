package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant,Long> {
    @Query("SELECT pv.id FROM ProductVariant pv " +
            "WHERE pv.product.id = :productId " +
            "AND EXISTS (" +
            "    SELECT 1 FROM ProductVariantAttribute pva " +
            "    WHERE pva.productVariant = pv " +
            "    AND pva.productAttributeValue.id IN :attributeValueIds " +
            "    GROUP BY pva.productVariant " +
            "    HAVING COUNT(DISTINCT pva.productAttributeValue.id) = :attributeValueCount" +
            ") " +
            "AND (" +
            "    SELECT COUNT(*) FROM ProductVariantAttribute pva " +
            "    WHERE pva.productVariant = pv" +
            ") = :attributeValueCount")
    Optional<Long> findVariantIdByProductIdAndAttributeValueIds(
            @Param("productId") Long productId,
            @Param("attributeValueIds") List<Long> attributeValueIds,
            @Param("attributeValueCount") Long attributeValueCount
    );
    @Modifying
    @Query("DELETE FROM ProductVariant")
    void deleteAllVariants();
    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.productVariantAttributes pva WHERE v.product.id = :productId")
    List<ProductVariant> findByProductIdWithAttributes(@Param("productId") Long productId);
}
