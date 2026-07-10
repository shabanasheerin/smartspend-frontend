package com.smartspend.mapper;

import com.smartspend.dto.expense.ExpenseResponse;
import com.smartspend.entity.Expense;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ExpenseResponse toResponse(Expense expense);
}
