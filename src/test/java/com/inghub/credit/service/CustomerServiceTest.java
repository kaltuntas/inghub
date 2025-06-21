package com.inghub.credit.service;

import com.inghub.credit.domain.Customer;
import com.inghub.credit.exception.ResourceNotFoundException;
import com.inghub.credit.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_CustomerExists_ReturnsCustomer() {
        Long customerId = 1L;
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        Customer result = customerService.findById(customerId);

        assertNotNull(result);
        assertEquals(customerId, result.getId());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void findById_CustomerDoesNotExist_ThrowsException() {

        Long customerId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> customerService.findById(customerId));
        assertEquals("Customer not found with given id: " + customerId, exception.getMessage());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void checkCustomerHasEnoughLimitToGetNewLoan_SufficientLimit_DoesNotThrowException() {

        Long customerId = 1L;
        BigDecimal newLoanAmount = BigDecimal.valueOf(500);
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setCreditLimit(BigDecimal.valueOf(1000));
        mockCustomer.setUsedCreditLimit(BigDecimal.valueOf(400));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));


        assertDoesNotThrow(() -> customerService.checkCustomerHasEnoughLimitToGetNewLoan(customerId, newLoanAmount));
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void checkCustomerHasEnoughLimitToGetNewLoan_InsufficientLimit_ThrowsException() {

        Long customerId = 1L;
        BigDecimal newLoanAmount = BigDecimal.valueOf(700);
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setCreditLimit(BigDecimal.valueOf(1000));
        mockCustomer.setUsedCreditLimit(BigDecimal.valueOf(400));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> customerService.checkCustomerHasEnoughLimitToGetNewLoan(customerId, newLoanAmount));
        assertEquals("Insufficient credit limit.", exception.getMessage());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void decreaseCustomerUsedCreditLimit_UpdatesUsedCreditLimit() {

        Long customerId = 1L;
        BigDecimal closedLoanAmount = BigDecimal.valueOf(200);
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setUsedCreditLimit(BigDecimal.valueOf(500));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        customerService.decreaseCustomerUsedCreditLimit(customerId, closedLoanAmount);

        assertEquals(BigDecimal.valueOf(300), mockCustomer.getUsedCreditLimit());
        verify(customerRepository, times(1)).save(mockCustomer);
    }

    @Test
    void increaseCustomerUsedCreditLimit_UpdatesUsedCreditLimit() {

        Long customerId = 1L;
        BigDecimal usedLoanAmount = BigDecimal.valueOf(200);
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setUsedCreditLimit(BigDecimal.valueOf(500));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        customerService.increaseCustomerUsedCreditLimit(customerId, usedLoanAmount);

        assertEquals(BigDecimal.valueOf(700), mockCustomer.getUsedCreditLimit());
        verify(customerRepository, times(1)).save(mockCustomer);
    }
}
