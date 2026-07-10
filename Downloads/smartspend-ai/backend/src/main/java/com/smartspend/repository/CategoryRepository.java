package com.smartspend.repository;

import com.smartspend.entity.Category;
import com.smartspend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findBySystemDefinedTrueOrUser(User user);
    List<Category> findByType(Category.CategoryType type);
}
