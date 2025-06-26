package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.Category;
import com.tawn.tawnht.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {

    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.productVariants pv " +
            "LEFT JOIN FETCH pv.productVariantAttributes pva " +
            "LEFT JOIN FETCH pva.productAttributeValue pav " +
            "LEFT JOIN FETCH pav.productAttribute pa " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.id = :id")
    Optional<Product> findProductById(@Param("id") Long id);

    Page<Product> findAllByCategory(Pageable pageable, Category category);


}
