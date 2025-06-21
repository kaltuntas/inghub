package com.inghub.credit.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inghub.credit.model.dto.LoanInstallmentDTO;

import java.util.List;


public record ListLoanInstallmentsResponse(long loanId,
                                           List<LoanInstallmentDTO> loanInstallments,
                                           @JsonProperty("paging") ApiModelPage apiModelPage) {

}