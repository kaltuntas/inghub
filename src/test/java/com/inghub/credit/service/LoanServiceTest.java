package com.inghub.credit.service;

import com.inghub.credit.domain.Customer;
import com.inghub.credit.domain.Loan;
import com.inghub.credit.exception.ResourceNotFoundException;
import com.inghub.credit.repository.LoanRepository;
import com.inghub.credit.model.CreateLoanResponse;
import com.inghub.credit.model.ListLoanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanServiceTest {

    @InjectMocks
    private LoanService loanService;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerService customerService;

    @Mock
    private LoanInstallmentService loanInstallmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindById_Success() {
        Customer customer = new Customer();
        customer.setId(1L);
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setCustomer(customer);

        when(loanRepository.findById(1L)).thenReturn(java.util.Optional.of(loan));

        Loan result = loanService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(loanRepository, times(1)).findById(1L);
    }

    @Test
    void testFindById_LoanNotFound() {
        when(loanRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loanService.findById(1L));

        verify(loanRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateLoan_Success() {
        Customer customer = new Customer();
        customer.setId(1L);
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setCustomer(customer);
        loan.setLoanAmount(new BigDecimal("100.00"));

        when(customerService.findById(1L)).thenReturn(customer);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        CreateLoanResponse response = loanService.createLoan(1L, BigDecimal.valueOf(1000), 12, BigDecimal.valueOf(0.1));

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(1000.0, response.loanAmount());
        assertEquals(12, response.numberOfInstallment());

        verify(customerService, times(1)).findById(1L);
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void testSearchLoansByCustomerId_Success() {
        Customer customer = new Customer();
        customer.setId(1L);

        Loan loan1 = new Loan();
        loan1.setId(1L);
        loan1.setCustomer(customer);
        loan1.setLoanAmount(new BigDecimal("100.00"));
        loan1.setNumberOfInstallment(12);

        Loan loan2 = new Loan();
        loan2.setId(2L);
        loan2.setCustomer(customer);
        loan2.setLoanAmount(new BigDecimal("200.00"));
        loan2.setNumberOfInstallment(6);

        List<Loan> loans = Arrays.asList(loan1, loan2);
        when(loanRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(new org.springframework.data.domain.PageImpl<>(loans));

        ListLoanResponse response = loanService.searchLoansByCustomerId(1L, BigDecimal.valueOf(1000), 12, false, 10, 1, "+id");

        assertNotNull(response);
        assertEquals(2, response.loans().size());
        assertEquals(1, response.apiModelPage().pageNumber());
        assertEquals(10, response.apiModelPage().pageSize());

        verify(loanRepository, times(1)).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void testUpdateLoanIsPaidStatus_Success() {
        Loan loan = new Loan();
        loan.setId(1L);

        when(loanRepository.findById(1L)).thenReturn(java.util.Optional.of(loan));

        loanService.updateLoanIsPaidStatus(1L, true);

        assertTrue(loan.isPaid());
        verify(loanRepository, times(1)).save(loan);
    }

    @Test
    void testUpdateLoanIsPaidStatus_LoanNotFound() {
        when(loanRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loanService.updateLoanIsPaidStatus(1L, true));

        verify(loanRepository, times(1)).findById(1L);
    }
}
