package com.smartspend.mapper;

import com.smartspend.dto.income.IncomeResponse;
import com.smartspend.entity.Income;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IncomeMapper {
    IncomeResponse toResponse(Income income);
}
