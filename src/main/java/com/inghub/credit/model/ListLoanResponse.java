package com.inghub.credit.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inghub.credit.model.dto.LoanDTO;

import java.util.List;


public record ListLoanResponse(List<LoanDTO> loans,
                               @JsonProperty("paging") ApiModelPage apiModelPage) {

}