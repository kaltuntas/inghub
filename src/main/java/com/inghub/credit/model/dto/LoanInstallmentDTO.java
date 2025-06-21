package com.inghub.credit.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LoanInstallmentDTO(Long id, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime insertDate,
                                 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updateDate,
                                 double amount, double paidAmount,
                                 @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dueDate,
                                 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime paymentDate,
                                 boolean isPaid) {

}