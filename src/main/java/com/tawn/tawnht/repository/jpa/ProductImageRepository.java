package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {
    @Modifying
    @Query("DELETE FROM ProductImage")
    void deleteAllImages();
}
