package com.inghub.credit.model;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateLoanRequest(@NotNull Long customerId,
                                @NotNull @DecimalMin("1.00") @DecimalMax("1000000.00") BigDecimal loanAmount,
                                @NotNull @Min(1) @Max(120) Integer numberOfInstallment,
                                @NotNull @DecimalMin("0.01") BigDecimal interestRate) {
}