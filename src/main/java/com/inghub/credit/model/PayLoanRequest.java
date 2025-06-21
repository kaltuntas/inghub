package com.inghub.credit.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PayLoanRequest(@NotNull Long loanId,
                             @NotNull @DecimalMin("1.00") @DecimalMax("1000000.00") BigDecimal paidAmount) {
}