package com.inghub.credit.service;

import com.inghub.credit.domain.Customer;
import com.inghub.credit.domain.Loan;
import com.inghub.credit.domain.LoanInstallment;
import com.inghub.credit.exception.CreditException;
import com.inghub.credit.exception.ResourceNotFoundException;
import com.inghub.credit.model.PayLoanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanPaymentService Tests")
class LoanPaymentServiceTest {

    @Mock
    private LoanService loanService;

    @Mock
    private LoanInstallmentService loanInstallmentService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private LoanPaymentService loanPaymentService;

    private Customer testCustomer;
    private Loan testLoan;
    private LoanInstallment testInstallment1;
    private LoanInstallment testInstallment2;
    private LoanInstallment testInstallment3;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("John");
        testCustomer.setSurname("Doe");
        testCustomer.setCreditLimit(BigDecimal.valueOf(10000));
        testCustomer.setUsedCreditLimit(BigDecimal.valueOf(5000));

        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setCustomer(testCustomer);
        testLoan.setLoanAmount(BigDecimal.valueOf(3000));
        testLoan.setInterestRate(BigDecimal.valueOf(0.1));
        testLoan.setNumberOfInstallment(3);
        testLoan.setPaid(false);

        testInstallment1 = new LoanInstallment();
        testInstallment1.setId(1L);
        testInstallment1.setLoan(testLoan);
        testInstallment1.setAmount(BigDecimal.valueOf(1000));
        testInstallment1.setPaidAmount(BigDecimal.ZERO);
        testInstallment1.setDueDate(LocalDate.now().plusMonths(1));
        testInstallment1.setPaid(false);

        testInstallment2 = new LoanInstallment();
        testInstallment2.setId(2L);
        testInstallment2.setLoan(testLoan);
        testInstallment2.setAmount(BigDecimal.valueOf(1000));
        testInstallment2.setPaidAmount(BigDecimal.ZERO);
        testInstallment2.setDueDate(LocalDate.now().plusMonths(2));
        testInstallment2.setPaid(false);

        testInstallment3 = new LoanInstallment();
        testInstallment3.setId(3L);
        testInstallment3.setLoan(testLoan);
        testInstallment3.setAmount(BigDecimal.valueOf(1000));
        testInstallment3.setPaidAmount(BigDecimal.ZERO);
        testInstallment3.setDueDate(LocalDate.now().plusMonths(3));
        testInstallment3.setPaid(false);
    }

    @Test
    @DisplayName("Should successfully pay single installment")
    void shouldSuccessfullyPaySingleInstallment() {
        // Given
        Long loanId = 1L;
        BigDecimal paidAmount = BigDecimal.valueOf(1000);
        List<LoanInstallment> unpaidInstallments = Arrays.asList(testInstallment1, testInstallment2, testInstallment3);
        List<LoanInstallment> eligibleInstallments = Collections.singletonList(testInstallment1);

        when(loanInstallmentService.findLoanInstallmentsByLoanIdAndIsPaid(loanId, false))
                .thenReturn(unpaidInstallments);
        when(loanInstallmentService.findEligibleInstallments(unpaidInstallments, paidAmount))
                .thenReturn(eligibleInstallments);

        // When
        PayLoanResponse response = loanPaymentService.payLoan(loanId, paidAmount);

        // Then
        assertNotNull(response);
        assertEquals(loanId, response.loanId());
        assertEquals(1, response.paidInstallmentCount());
        assertEquals(1000.0, response.totalAmountSpent());
        assertFalse(response.loanPaidCompletely());

        verify(loanInstallmentService).payMultipleLoanInstallments(Collections.singletonList(1L));
        verify(loanService, never()).updateLoanIsPaidStatus(anyLong(), anyBoolean());
        verify(customerService).decreaseCustomerUsedCreditLimit(1L, BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Should successfully pay multiple installments")
    void shouldSuccessfullyPayMultipleInstallments() {
        // Given
        Long loanId = 1L;
        BigDecimal paidAmount = BigDecimal.valueOf(2500);
        List<LoanInstallment> unpaidInstallments = Arrays.asList(testInstallment1, testInstallment2, testInstallment3);
        List<LoanInstallment> eligibleInstallments = Arrays.asList(testInstallment1, testInstallment2);

        when(loanInstallmentService.findLoanInstallmentsByLoanIdAndIsPaid(loanId, false))
                .thenReturn(unpaidInstallments);
        when(loanInstallmentService.findEligibleInstallments(unpaidInstallments, paidAmount))
                .thenReturn(eligibleInstallments);

        // When
        PayLoanResponse response = loanPaymentService.payLoan(loanId, paidAmount);

        // Then
        assertNotNull(response);
        assertEquals(loanId, response.loanId());
        assertEquals(2, response.paidInstallmentCount());
        assertEquals(2000.0, response.totalAmountSpent());
        assertFalse(response.loanPaidCompletely());

        verify(loanInstallmentService).payMultipleLoanInstallments(Arrays.asList(1L, 2L));
        verify(loanService, never()).updateLoanIsPaidStatus(anyLong(), anyBoolean());
        verify(customerService).decreaseCustomerUsedCreditLimit(1L, BigDecimal.valueOf(2000));
    }

    @Test
    @DisplayName("Should successfully pay all installments and mark loan as paid")
    void shouldSuccessfullyPayAllInstallmentsAndMarkLoanAsPaid() {
        // Given
        Long loanId = 1L;
        BigDecimal paidAmount = BigDecimal.valueOf(3000);
        List<LoanInstallment> unpaidInstallments = Arrays.asList(testInstallment1, testInstallment2, testInstallment3);
        List<LoanInstallment> eligibleInstallments = Arrays.asList(testInstallment1, testInstallment2, testInstallment3);

        when(loanInstallmentService.findLoanInstallmentsByLoanIdAndIsPaid(loanId, false))
                .thenReturn(unpaidInstallments);
        when(loanInstallmentService.findEligibleInstallments(unpaidInstallments, paidAmount))
                .thenReturn(eligibleInstallments);

        // When
        PayLoanResponse response = loanPaymentService.payLoan(loanId, paidAmount);

        // Then
        assertNotNull(response);
        assertEquals(loanId, response.loanId());
        assertEquals(3, response.paidInstallmentCount());
        assertEquals(3000.0, response.totalAmountSpent());
        assertTrue(response.loanPaidCompletely());

        verify(loanInstallmentService).payMultipleLoanInstallments(Arrays.asList(1L, 2L, 3L));
        verify(loanService).updateLoanIsPaidStatus(loanId, true);
        verify(customerService).decreaseCustomerUsedCreditLimit(1L, BigDecimal.valueOf(3000));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no unpaid installments exist")
    void shouldThrowResourceNotFoundExceptionWhenNoUnpaidInstallments() {
        // Given
        Long loanId = 1L;
        BigDecimal paidAmount = BigDecimal.valueOf(1000);

        when(loanInstallmentService.findLoanInstallmentsByLoanIdAndIsPaid(loanId, false))
                .thenReturn(Collections.emptyList());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> loanPaymentService.payLoan(loanId, paidAmount)
        );

        assertEquals("Unpaid installment could not found for given loan id: " + loanId, exception.getMessage());

        verify(loanInstallmentService, never()).findEligibleInstallments(any(), any());
        verify(loanInstallmentService, never()).payMultipleLoanInstallments(any());
        verify(loanService, never()).updateLoanIsPaidStatus(anyLong(), anyBoolean());
        verify(customerService, never()).decreaseCustomerUsedCreditLimit(anyLong(), any());
    }

    @Test
    @DisplayName("Should throw CreditException when no eligible installments for payment")
    void shouldThrowCreditExceptionWhenNoEligibleInstallments() {
        // Given
        Long loanId = 1L;
        BigDecimal paidAmount = BigDecimal.valueOf(500); // Less than installment amount
        List<LoanInstallment> unpaidInstallments = Collections.singletonList(testInstallment1);

        when(loanInstallmentService.findLoanInstallmentsByLoanIdAndIsPaid(loanId, false))
                .thenReturn(unpaidInstallments);
        when(loanInstallmentService.findEligibleInstallments(unpaidInstallments, paidAmount))
                .thenReturn(Collections.emptyList());

        // When & Then
        CreditException exception = assertThrows(
                CreditException.class,
                () -> loanPaymentService.payLoan(loanId, paidAmount)
        );

        assertEquals("No installments are eligible for payment for loanId: " + loanId, exception.getMessage());

        verify(loanInstallmentService, never()).payMultipleLoanInstallments(any());
        verify(loanService, never()).updateLoanIsPaidStatus(anyLong(), anyBoolean());
        verify(customerService, never()).decreaseCustomerUsedCreditLimit(anyLong(), any());
    }

    @Test
    @DisplayName("Should throw CreditException when payment amount is less than installment amount")
    void shouldThrowCreditExceptionWhenPaymentAmountLessThanInstallmentAmount() {
        // Given
        Long loanId = 1L;
        BigDecimal paidAmount = BigDecimal.valueOf(500); // Less than 1000
        List<LoanInstallment> unpaidInstallments = Collections.singletonList(testInstallment1);
        List<LoanInstallment> eligibleInstallments = Collections.singletonList(testInstallment1);

        when(loanInstallmentService.findLoanInstallmentsByLoanIdAndIsPaid(loanId, false))
                .thenReturn(unpaidInstallments);
        when(loanInstallmentService.findEligibleInstallments(unpaidInstallments, paidAmount))
                .thenReturn(eligibleInstallments);

        // When & Then
        CreditException exception = assertThrows(
                CreditException.class,
                () -> loanPaymentService.payLoan(loanId, paidAmount)
        );

        assertEquals("Installment amount exceeds paid amount: 1000", exception.getMessage());

        verify(loanInstallmentService, never()).payMultipleLoanInstallments(any());
        verify(loanService, never()).updateLoanIsPaidStatus(anyLong(), anyBoolean());
        verify(customerService, never()).decreaseCustomerUsedCreditLimit(anyLong(), any());
    }

    @Test
    @DisplayName("Should handle exact payment amount matching installment amount")
    void shouldHandleExactPaymentAmountMatchingInstallmentAmount() {
        // Given
        Long loanId = 1L;
        BigDecimal paidAmount = BigDecimal.valueOf(1000); // Exactly matches installment amount
        List<LoanInstallment> unpaidInstallments = Arrays.asList(testInstallment1, testInstallment2);
        List<LoanInstallment> eligibleInstallments = Collections.singletonList(testInstallment1);

        when(loanInstallmentService.findLoanInstallmentsByLoanIdAndIsPaid(loanId, false))
                .thenReturn(unpaidInstallments);
        when(loanInstallmentService.findEligibleInstallments(unpaidInstallments, paidAmount))
                .thenReturn(eligibleInstallments);

        // When
        PayLoanResponse response = loanPaymentService.payLoan(loanId, paidAmount);

        // Then
        assertNotNull(response);
        assertEquals(1, response.paidInstallmentCount());
        assertEquals(1000.0, response.totalAmountSpent());
        assertFalse(response.loanPaidCompletely());
    }

    @Test
    @DisplayName("Should handle single installment loan payment")
    void shouldHandleSingleInstallmentLoanPayment() {
        // Given
        Long loanId = 1L;
        BigDecimal paidAmount = BigDecimal.valueOf(1000);
        List<LoanInstallment> unpaidInstallments = Collections.singletonList(testInstallment1);
        List<LoanInstallment> eligibleInstallments = Collections.singletonList(testInstallment1);

        when(loanInstallmentService.findLoanInstallmentsByLoanIdAndIsPaid(loanId, false))
                .thenReturn(unpaidInstallments);
        when(loanInstallmentService.findEligibleInstallments(unpaidInstallments, paidAmount))
                .thenReturn(eligibleInstallments);

        // When
        PayLoanResponse response = loanPaymentService.payLoan(loanId, paidAmount);

        // Then
        assertNotNull(response);
        assertEquals(1, response.paidInstallmentCount());
        assertEquals(1000.0, response.totalAmountSpent());
        assertTrue(response.loanPaidCompletely()); // All installments paid

        verify(loanService).updateLoanIsPaidStatus(loanId, true);
    }

    // Static method tests
    @Test
    @DisplayName("checkPaymentAmountMoreThanInstallmentAmount - Should pass when payment amount equals installment amount")
    void checkPaymentAmount_ShouldPassWhenPaymentAmountEqualsInstallmentAmount() {
        // Given
        BigDecimal installmentAmount = BigDecimal.valueOf(1000);
        BigDecimal paidAmount = BigDecimal.valueOf(1000);

        // When & Then
        assertDoesNotThrow(() ->
                                   LoanPaymentService.checkPaymentAmountMoreThanInstallmentAmount(installmentAmount, paidAmount)
        );
    }

    @Test
    @DisplayName("checkPaymentAmountMoreThanInstallmentAmount - Should pass when payment amount is greater than installment amount")
    void checkPaymentAmount_ShouldPassWhenPaymentAmountIsGreaterThanInstallmentAmount() {
        // Given
        BigDecimal installmentAmount = BigDecimal.valueOf(1000);
        BigDecimal paidAmount = BigDecimal.valueOf(1500);

        // When & Then
        assertDoesNotThrow(() ->
                                   LoanPaymentService.checkPaymentAmountMoreThanInstallmentAmount(installmentAmount, paidAmount)
        );
    }

    @Test
    @DisplayName("checkPaymentAmountMoreThanInstallmentAmount - Should throw CreditException when payment amount is less than installment amount")
    void checkPaymentAmount_ShouldThrowCreditExceptionWhenPaymentAmountIsLessThanInstallmentAmount() {
        // Given
        BigDecimal installmentAmount = BigDecimal.valueOf(1000);
        BigDecimal paidAmount = BigDecimal.valueOf(500);

        // When & Then
        CreditException exception = assertThrows(CreditException.class, () ->
                LoanPaymentService.checkPaymentAmountMoreThanInstallmentAmount(installmentAmount, paidAmount)
        );

        assertEquals("Installment amount exceeds paid amount: 1000", exception.getMessage());
    }

    @Test
    @DisplayName("checkPaymentAmountMoreThanInstallmentAmount - Should throw IllegalArgumentException when payment amount is negative")
    void checkPaymentAmount_ShouldThrowIllegalArgumentExceptionWhenPaymentAmountIsNegative() {
        // Given
        BigDecimal installmentAmount = BigDecimal.valueOf(1000);
        BigDecimal paidAmount = BigDecimal.valueOf(-100);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                LoanPaymentService.checkPaymentAmountMoreThanInstallmentAmount(installmentAmount, paidAmount)
        );

        assertEquals("Payment amount cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("checkPaymentAmountMoreThanInstallmentAmount - Should handle zero payment amount")
    void checkPaymentAmount_ShouldHandleZeroPaymentAmount() {
        // Given
        BigDecimal installmentAmount = BigDecimal.valueOf(1000);
        BigDecimal paidAmount = BigDecimal.ZERO;

        // When & Then
        CreditException exception = assertThrows(CreditException.class, () ->
                LoanPaymentService.checkPaymentAmountMoreThanInstallmentAmount(installmentAmount, paidAmount)
        );

        assertEquals("Installment amount exceeds paid amount: 1000", exception.getMessage());
    }

    @Test
    @DisplayName("checkPaymentAmountMoreThanInstallmentAmount - Should handle zero installment amount")
    void checkPaymentAmount_ShouldHandleZeroInstallmentAmount() {
        // Given
        BigDecimal installmentAmount = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.valueOf(100);

        // When & Then
        assertDoesNotThrow(() ->
                                   LoanPaymentService.checkPaymentAmountMoreThanInstallmentAmount(installmentAmount, paidAmount)
        );
    }
}
