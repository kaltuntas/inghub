package com.inghub.credit.model;

public record PayLoanResponse(Long loanId,
                              int paidInstallmentCount,
                              double totalAmountSpent,
                              boolean loanPaidCompletely) {

}
