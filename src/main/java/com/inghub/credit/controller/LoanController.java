package com.inghub.credit.controller;

import com.inghub.credit.model.CreateLoanRequest;
import com.inghub.credit.model.PayLoanRequest;
import com.inghub.credit.model.CreateLoanResponse;
import com.inghub.credit.model.ListLoanInstallmentsResponse;
import com.inghub.credit.model.ListLoanResponse;
import com.inghub.credit.model.PayLoanResponse;
import com.inghub.credit.service.LoanInstallmentService;
import com.inghub.credit.service.LoanPaymentService;
import com.inghub.credit.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final LoanInstallmentService loanInstallmentService;
    private final LoanPaymentService loanPaymentService;


    //list loans of customer
    @GetMapping("/loans")
    public ResponseEntity<ListLoanResponse> getLoansOfCustomer(@Valid @RequestParam("customerId") Long customerId,
                                                               @Valid @RequestParam(value = "loanAmount", required = false) BigDecimal loanAmount,
                                                               @Valid @RequestParam(value = "installmentCount", required = false) Integer installmentCount,
                                                               @Valid @RequestParam(value = "isPaid", required = false) Boolean paid,
                                                               @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                               @Valid @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                                               @Valid @RequestParam(value = "sort", required = false) String sort) {

        ListLoanResponse loans = loanService.searchLoansByCustomerId(customerId, loanAmount, installmentCount, paid, pageSize, pageNumber, sort);
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    //list installments for loan
    @GetMapping("/loans/{loanId}/installments")
    public ResponseEntity<ListLoanInstallmentsResponse> getLoanInstallments(@PathVariable("loanId") Long loanId,
                                                                            @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                                            @Valid @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                                                                            @Valid @RequestParam(value = "sort", required = false) String sort) {

        ListLoanInstallmentsResponse loanInstallments = loanInstallmentService.searchLoanInstallmentsByLoanId(loanId, pageSize, pageNumber, sort);
        return new ResponseEntity<>(loanInstallments, HttpStatus.OK);
    }

    //create loan for customer
    @PostMapping("/loans")
    public ResponseEntity<CreateLoanResponse> createLoan(@Valid @RequestBody CreateLoanRequest request, BindingResult bindingResult,
                                                         UriComponentsBuilder uriComponentsBuilder) throws BindException {

        if (bindingResult.hasErrors() || (request == null)) {
            throw new BindException(bindingResult);
        }

        CreateLoanResponse response = loanService.createLoan(request.customerId(), request.loanAmount(), request.numberOfInstallment(), request.interestRate());

        //TODO: create GET loans/{id} api
        return ResponseEntity
                .created(uriComponentsBuilder.path("/api/v1/credit/loans/{id}").build(response.id()))
                .body(response);
    }

    @PostMapping("/loans/pay")
    public ResponseEntity<PayLoanResponse> payLoan(@Valid @RequestBody PayLoanRequest request, BindingResult bindingResult) throws BindException {

        if (bindingResult.hasErrors() || (request == null)) {
            throw new BindException(bindingResult);
        }

        PayLoanResponse response = loanPaymentService.payLoan(request.loanId(), request.paidAmount());


        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
