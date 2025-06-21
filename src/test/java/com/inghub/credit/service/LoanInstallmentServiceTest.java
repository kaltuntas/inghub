package com.inghub.credit.service;

import com.inghub.credit.domain.Loan;
import com.inghub.credit.domain.LoanInstallment;
import com.inghub.credit.exception.ResourceNotFoundException;
import com.inghub.credit.repository.LoanInstallmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanInstallmentServiceTest {

    @InjectMocks
    private LoanInstallmentService loanInstallmentService;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @BeforeEach
    void setUp() {
        // Called before each test - Initialize mocks
    }

    private LoanInstallment createInstallmentWithDueDate(Long id, BigDecimal amount, LocalDate dueDate) {
        LoanInstallment installment = new LoanInstallment();
        installment.setId(id);
        installment.setAmount(amount);
        installment.setDueDate(dueDate);
        return installment;
    }


    @Test
    void testFindById_Success() {
        LoanInstallment installment = createDummyLoanInstallment();
        when(loanInstallmentRepository.findById(1L)).thenReturn(Optional.of(installment));

        LoanInstallment result = loanInstallmentService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(loanInstallmentRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_NotFound() {
        when(loanInstallmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loanInstallmentService.findById(1L));
        verify(loanInstallmentRepository, times(1)).findById(1L);
    }

    @Test
    void testGetPaginatedLoanInstallmentsByLoanId() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<LoanInstallment> installments = List.of(createDummyLoanInstallment());
        Page<LoanInstallment> page = new PageImpl<>(installments);
        when(loanInstallmentRepository.findByLoanId(1L, pageRequest)).thenReturn(page);

        Page<LoanInstallment> result = loanInstallmentService.getPaginatedLoanInstallmentsByLoanId(1L, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(loanInstallmentRepository, times(1)).findByLoanId(1L, pageRequest);
    }

    @Test
    void testCreateLoanInstallments() {
        Loan loan = new Loan();
        loan.setId(1L);
        BigDecimal loanAmount = BigDecimal.valueOf(1000);
        int numberOfInstallments = 3;
        BigDecimal interestRate = BigDecimal.valueOf(0.1);
        List<LocalDate> dates = LoanInstallmentService.createInstallmentDatesByInstallmentCount(numberOfInstallments);

        List<LoanInstallment> result = loanInstallmentService.createLoanInstallments(loan, loanAmount, numberOfInstallments, dates, interestRate);

        assertEquals(numberOfInstallments, result.size());
        verify(loanInstallmentRepository, never()).save(any());
    }

    @Test
    void testPayMultipleLoanInstallments() {
        LoanInstallment installment = createDummyLoanInstallment();
        installment.setPaid(false);
        when(loanInstallmentRepository.findById(installment.getId())).thenReturn(Optional.of(installment));

        loanInstallmentService.payMultipleLoanInstallments(Collections.singletonList(installment.getId()));

        assertTrue(installment.isPaid());
        assertEquals(installment.getAmount(), installment.getPaidAmount());
        verify(loanInstallmentRepository, times(1)).save(installment);
    }

    @Test
    void testCheckInstallmentDueDateValidation() {
        boolean result = LoanInstallmentService.checkInstallmentHaveDueDateMoreThanGivenDurationInMonths(LocalDate.now().plusMonths(4), 3);

        assertTrue(result);
    }

    // Helper method to create a dummy LoanInstallment
    private LoanInstallment createDummyLoanInstallment() {
        LoanInstallment installment = new LoanInstallment();
        installment.setId(1L);
        installment.setAmount(BigDecimal.valueOf(500));
        installment.setDueDate(LocalDate.now().plusDays(30));
        installment.setPaid(false);
        return installment;
    }

    @Test
    void testCalculateInstallmentAmount_ShouldHandleRoundingUp() {
        BigDecimal totalAmountToBePaid = BigDecimal.valueOf(1001);
        int numberOfInstallments = 3;

        BigDecimal result = LoanInstallmentService.calculateInstallmentAmount(totalAmountToBePaid, numberOfInstallments);

        // Expected value is 333.67 after rounding up
        assertEquals(0, result.compareTo(BigDecimal.valueOf(333.67)));
    }

    @Test
    void testCalculateTotalAmountToBePaid_ShouldIncludeInterest() {
        BigDecimal loanAmount = BigDecimal.valueOf(1000);
        BigDecimal interestRate = BigDecimal.valueOf(0.05); // 5% interest rate
        int numberOfInstallments = 5;

        BigDecimal result = LoanInstallmentService.calculateTotalAmountToBePaid(loanAmount, numberOfInstallments, interestRate);

        // Loan amount (1000) + interest (5%) = 1050.00
        assertEquals(0, result.compareTo(BigDecimal.valueOf(1050.00)));
    }

    @Test
    void testCreateInstallmentDatesByInstallmentCount_ShouldHandleBoundaryCases() {
        // Case where only 1 installment is created
        List<LocalDate> dates = LoanInstallmentService.createInstallmentDatesByInstallmentCount(1);

        assertEquals(1, dates.size());
        assertTrue(dates.get(0).isAfter(LocalDate.now()));
    }

    @Test
    void testPayMultipleLoanInstallments_ShouldHandleEmptyIds() {
        List<Long> installmentIds = Collections.emptyList();

        loanInstallmentService.payMultipleLoanInstallments(installmentIds);

        // Verify no repository methods are called
        verify(loanInstallmentRepository, never()).save(any());
    }

    @Test
    void testFindEligibleInstallments_ShouldConsiderDueDates() {
        LocalDate today = LocalDate.now();

        LoanInstallment installment1 = createInstallmentWithDueDate(1L, BigDecimal.valueOf(500), today.plusMonths(2));
        LoanInstallment installment2 = createInstallmentWithDueDate(2L, BigDecimal.valueOf(500), today.plusMonths(4)); // Past 3-month boundary
        LoanInstallment installment3 = createInstallmentWithDueDate(3L, BigDecimal.valueOf(500), today.plusMonths(3)); // On boundary

        List<LoanInstallment> installments = List.of(installment1, installment2, installment3);

        List<LoanInstallment> result = loanInstallmentService.findEligibleInstallments(installments, BigDecimal.valueOf(1000));

        // Only installment1 should be in the result (installment2 is beyond the 3-month boundary)
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testCheckNumberOfInstallmentIsValid_ShouldThrowForInvalidNumber() {
        int invalidInstallmentCount = 7;

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                LoanInstallmentService.checkNumberOfInstallmentIsValid(invalidInstallmentCount)
        );

        assertEquals("Invalid number of installments. Must be: [6, 9, 12, 24]", exception.getMessage());
    }

    @Test
    void testCheckInstallmentHaveDueDateMoreThanGivenDurationInMonths_ShouldReturnFalseForPastDate() {
        LocalDate pastDate = LocalDate.now().minusMonths(1);

        boolean result = LoanInstallmentService.checkInstallmentHaveDueDateMoreThanGivenDurationInMonths(pastDate, 3);

        assertFalse(result);
    }

    @Test
    void testGetPaginatedLoanInstallmentsByLoanId_ShouldReturnEmptyPage() {
        Page<LoanInstallment> emptyPage = Page.empty();
        PageRequest pageRequest = PageRequest.of(0, 10);

        when(loanInstallmentRepository.findByLoanId(1L, pageRequest)).thenReturn(emptyPage);

        Page<LoanInstallment> result = loanInstallmentService.getPaginatedLoanInstallmentsByLoanId(1L, pageRequest);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(loanInstallmentRepository, times(1)).findByLoanId(1L, pageRequest);
    }

    @Test
    void testValidateInstallmentAmounts_ShouldAdjustLastInstallment() {
        Loan loan = new Loan();
        loan.setId(1L);
        BigDecimal loanAmount = BigDecimal.valueOf(1000);
        int numberOfInstallments = 3;
        BigDecimal interestRate = BigDecimal.valueOf(0.05);

        List<LocalDate> installmentDates =
                LoanInstallmentService.createInstallmentDatesByInstallmentCount(numberOfInstallments);

        List<LoanInstallment> installments = loanInstallmentService.createLoanInstallments(
                loan, loanAmount, numberOfInstallments, installmentDates, interestRate
        );

        // Validate the total installment amount matches the calculated total
        BigDecimal totalInstallmentAmount = installments.stream()
                .map(LoanInstallment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expectedTotal = LoanInstallmentService.calculateTotalAmountToBePaid(loanAmount, numberOfInstallments, interestRate);

        assertEquals(0, totalInstallmentAmount.compareTo(expectedTotal));
    }

}