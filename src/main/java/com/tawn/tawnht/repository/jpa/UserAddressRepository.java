package com.tawn.tawnht.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tawn.tawnht.entity.User;
import com.tawn.tawnht.entity.UserAddress;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    Optional<UserAddress> findByIdAndStatus(Long id, String status);

    List<UserAddress> findAllByUserAndStatus(User user, String status);
}
