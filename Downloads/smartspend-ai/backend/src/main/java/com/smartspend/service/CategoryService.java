package com.smartspend.service;

import com.smartspend.dto.category.CategoryResponse;
import com.smartspend.entity.User;
import com.smartspend.mapper.CategoryMapper;
import com.smartspend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> listForUser(User user) {
        return categoryRepository.findBySystemDefinedTrueOrUser(user)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}
