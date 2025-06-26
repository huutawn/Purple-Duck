package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller,Long> {
    @Query("select s from Seller s where s.verifyToken = :token")
    Optional<Seller> findSellerByVerifyToken(@Param("token") String token);
    @Query("SELECT s FROM Seller s WHERE s.user.id = :id")
    Optional<Seller> findSellerByUser(@Param("id")String userId);
}
