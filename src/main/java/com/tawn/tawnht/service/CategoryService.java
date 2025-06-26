package com.tawn.tawnht.service;


import com.tawn.tawnht.entity.Category;

import com.tawn.tawnht.repository.jpa.CategoryRepository;
import com.tawn.tawnht.repository.jpa.UserRepository;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
public class CategoryService {
    UserRepository userRepository;
    CategoryRepository categoryRepository;
    public String createCategory(List<String> names){
        List<Category> categories = new ArrayList<>();
        for (String name : names) {
            Category category = new Category();
            category.setName(name);
            categories.add(category);
        }
        categoryRepository.saveAll(categories);
        return "Categories created successfully";
    }
    public List<Category> getAll(){
        return categoryRepository.findAll();
    }
}
