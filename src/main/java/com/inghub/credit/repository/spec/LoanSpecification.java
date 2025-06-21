package com.inghub.credit.repository.spec;

import com.inghub.credit.domain.Loan;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LoanSpecification {

    public static Specification<Loan> getSpecificationForLoan(Long customerId, BigDecimal loanAmount, Integer installmentCount, Boolean paid) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicateList = new ArrayList<>();

            // Join Loan with Customer
            Join<Object, Object> customerJoin = root.join("customer");

            if (customerId != null) {
                predicateList.add(criteriaBuilder.equal(customerJoin.get("id"), customerId));
            }

            if (loanAmount != null && loanAmount.compareTo(BigDecimal.ZERO) > 0) {
                predicateList.add(criteriaBuilder.equal(root.get("loanAmount"), loanAmount));
            }

            if (installmentCount != null && installmentCount > 0) {
                predicateList.add(criteriaBuilder.equal(root.get("numberOfInstallment"), installmentCount));
            }

            if (paid != null) {
                predicateList.add(criteriaBuilder.equal(root.get("paid"), paid));
            }

            return criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        };
    }
}