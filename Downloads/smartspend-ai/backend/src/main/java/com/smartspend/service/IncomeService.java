package com.smartspend.service;

import com.smartspend.dto.income.IncomeRequest;
import com.smartspend.dto.income.IncomeResponse;
import com.smartspend.entity.Income;
import com.smartspend.entity.User;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.mapper.IncomeMapper;
import com.smartspend.repository.IncomeRepository;
import com.smartspend.specification.IncomeSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;

    @Transactional
    public IncomeResponse create(User user, IncomeRequest request) {
        Income income = Income.builder()
                .user(user)
                .amount(request.getAmount())
                .source(request.getSource())
                .incomeDate(request.getIncomeDate())
                .notes(request.getNotes())
                .build();

        return incomeMapper.toResponse(incomeRepository.save(income));
    }

    @Transactional
    public IncomeResponse update(User user, Long incomeId, IncomeRequest request) {
        Income income = findOwned(user, incomeId);

        income.setAmount(request.getAmount());
        income.setSource(request.getSource());
        income.setIncomeDate(request.getIncomeDate());
        income.setNotes(request.getNotes());

        return incomeMapper.toResponse(incomeRepository.save(income));
    }

    @Transactional
    public void delete(User user, Long incomeId) {
        Income income = findOwned(user, incomeId);
        incomeRepository.delete(income);
    }

    public IncomeResponse getById(User user, Long incomeId) {
        return incomeMapper.toResponse(findOwned(user, incomeId));
    }

    public Page<IncomeResponse> search(User user,
                                        Income.IncomeSource source,
                                        LocalDate startDate,
                                        LocalDate endDate,
                                        BigDecimal minAmount,
                                        BigDecimal maxAmount,
                                        String search,
                                        Pageable pageable) {
        return incomeRepository
                .findAll(IncomeSpecification.filter(user, source, startDate, endDate, minAmount, maxAmount, search), pageable)
                .map(incomeMapper::toResponse);
    }

    public BigDecimal totalForPeriod(User user, LocalDate start, LocalDate end) {
        return incomeRepository.sumByUserAndDateRange(user, start, end);
    }

    private Income findOwned(User user, Long incomeId) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Income record not found"));

        if (!income.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Income record not found");
        }
        return income;
    }
}
