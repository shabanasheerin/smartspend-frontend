package com.smartspend.mapper;

import com.smartspend.dto.category.CategoryResponse;
import com.smartspend.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}
