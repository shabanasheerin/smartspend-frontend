package com.smartspend.dto.category;

import com.smartspend.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private Category.CategoryType type;
    private String icon;
    private String color;
    private boolean systemDefined;
}
