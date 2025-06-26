package com.tawn.tawnht.repository.jpa;

import com.tawn.tawnht.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress,Long> {
}
