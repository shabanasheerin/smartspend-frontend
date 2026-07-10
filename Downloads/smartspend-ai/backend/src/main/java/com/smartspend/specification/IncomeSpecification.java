package com.smartspend.specification;

import com.smartspend.entity.Income;
import com.smartspend.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IncomeSpecification {

    private IncomeSpecification() {
    }

    public static Specification<Income> filter(User user,
                                                Income.IncomeSource source,
                                                LocalDate startDate,
                                                LocalDate endDate,
                                                BigDecimal minAmount,
                                                BigDecimal maxAmount,
                                                String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("user"), user));

            if (source != null) {
                predicates.add(cb.equal(root.get("source"), source));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("incomeDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("incomeDate"), endDate));
            }
            if (minAmount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            if (maxAmount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }
            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("notes")), "%" + search.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
