package com.tawn.tawnht.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tawn.tawnht.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {}
