package com.inghub.credit.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record LoanDTO(Long id, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime insertDate,
                      @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updateDate,
                      long customerId,
                      double loanAmount,
                      int numberOfInstallment,
                      boolean isPaid) {

}