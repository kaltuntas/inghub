package com.inghub.credit.service;

import com.inghub.credit.domain.LoanInstallment;
import com.inghub.credit.exception.CreditException;
import com.inghub.credit.exception.ResourceNotFoundException;
import com.inghub.credit.model.PayLoanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanPaymentService {

    private final LoanService loanService;
    private final LoanInstallmentService loanInstallmentService;
    private final CustomerService customerService;

    @Transactional
    public PayLoanResponse payLoan(Long loanId, BigDecimal paidAmount) {
        log.info("Starting payLoan with loanId: {} and paidAmount: {}", loanId, paidAmount);

        List<LoanInstallment> unPaidInstallmentList = loanInstallmentService.findLoanInstallmentsByLoanIdAndIsPaid(loanId, false);
        if (unPaidInstallmentList.isEmpty()) {
            log.error("No unpaid installments found for loanId: {}", loanId);
            throw new ResourceNotFoundException("Unpaid installment could not found for given loan id: " + loanId);
        }

        //find installments to be paid with paid amount
        List<LoanInstallment> eligibleInstallmentList = loanInstallmentService.findEligibleInstallments(unPaidInstallmentList, paidAmount);
        if (eligibleInstallmentList.isEmpty()) {
            log.error("No installments are eligible for payment for loanId: {}", loanId);
            throw new CreditException("No installments are eligible for payment for loanId: " + loanId);
        }

        BigDecimal installmentAmount = unPaidInstallmentList.get(0).getAmount();
        log.debug("First unpaid installment amount for loanId {} is {}", loanId, installmentAmount);

        //Installments should be paid wholly or not at all.
        checkPaymentAmountMoreThanInstallmentAmount(installmentAmount, paidAmount);

        int eligibleInstallmentCount = eligibleInstallmentList.size();
        log.info("{} installments can be paid for loanId: {}", eligibleInstallmentCount, loanId);

        //pay eligible installments
        loanInstallmentService.payMultipleLoanInstallments(eligibleInstallmentList.stream().map(LoanInstallment::getId).collect(Collectors.toList()));
        log.info("Paid {} installments for loanId: {}", eligibleInstallmentCount, loanId);

        //if all installments are paid, update Loan entity
        boolean allInstallmentsPaid = unPaidInstallmentList.size() == eligibleInstallmentList.size();
        if (allInstallmentsPaid) {
            log.info("All installments paid for loanId: {}", loanId);
            loanService.updateLoanIsPaidStatus(loanId, true);
        }

        //update customer used credit limit according to paid installment amount
        long customerId = unPaidInstallmentList.get(0).getLoan().getCustomer().getId();
        BigDecimal totalDeductedAmount = installmentAmount.multiply(BigDecimal.valueOf(eligibleInstallmentCount));
        log.debug("Decreasing credit limit for customerId: {} by {}", customerId, totalDeductedAmount);
        customerService.decreaseCustomerUsedCreditLimit(customerId, totalDeductedAmount);

        log.info("Completed payLoan for loanId: {}", loanId);
        return new PayLoanResponse(loanId, eligibleInstallmentCount, totalDeductedAmount.doubleValue(), allInstallmentsPaid);
    }

    //Installments should be paid wholly or not at all.
    public static void checkPaymentAmountMoreThanInstallmentAmount(BigDecimal installmentAmount, BigDecimal paidAmount) {
        log.debug("Validating payment amount. Installment amount: {}, Paid amount: {}", installmentAmount, paidAmount);

        if (paidAmount.compareTo(BigDecimal.ZERO) < 0) {
            log.error("Payment amount is negative: {}", paidAmount);
            throw new IllegalArgumentException("Payment amount cannot be negative");
        }
        if (installmentAmount.compareTo(paidAmount) > 0) {
            log.error("Installment amount {} exceeds paid amount {}", installmentAmount, paidAmount);
            throw new CreditException("Installment amount exceeds paid amount: " + installmentAmount);
        }
    }


}
