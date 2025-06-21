package com.inghub.credit.service;

import com.inghub.credit.domain.Customer;
import com.inghub.credit.exception.ResourceNotFoundException;
import com.inghub.credit.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer findById(Long id) {
        log.info("Attempting to find Customer with ID: {}", id);
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isEmpty()) {
            log.error("Customer not found with given id: {}", id);
            throw new ResourceNotFoundException("Customer not found with given id: " + id);
        } else {
            log.info("Found Customer with ID: {}", id);
            return customer.get();
        }
    }

    public void checkCustomerHasEnoughLimitToGetNewLoan(Long customerId, BigDecimal newLoanAmount) {
        log.info("Checking if Customer with ID: {} has enough credit limit for new loan of amount: {}", customerId, newLoanAmount);
        Customer customer = findById(customerId);
        BigDecimal customerAvailableLimit = customer.getCreditLimit().subtract(customer.getUsedCreditLimit());
        if (customerAvailableLimit.compareTo(BigDecimal.ZERO) <= 0 || customerAvailableLimit.compareTo(newLoanAmount) < 0) {
            log.error("Customer with ID: {} has insufficient credit limit. Available: {}, Required: {}", customerId, customerAvailableLimit, newLoanAmount);
            throw new IllegalArgumentException("Insufficient credit limit.");
        }
        log.info("Customer with ID: {} has enough credit limit for the new loan.", customerId);
    }

    public void decreaseCustomerUsedCreditLimit(Long customerId, BigDecimal closedLoanAmount) {
        log.info("Decreasing the used credit limit for Customer with ID: {} by amount: {}", customerId, closedLoanAmount);
        Customer customer = findById(customerId);
        BigDecimal customerCurrentUsedLimit = customer.getUsedCreditLimit();
        BigDecimal customerNewUsedLimit = customerCurrentUsedLimit.subtract(closedLoanAmount);
        customer.setUsedCreditLimit(customerNewUsedLimit);
        customerRepository.save(customer);
        log.info("Customer with ID: {} has new used credit limit: {}", customerId, customerNewUsedLimit);
    }

    public void increaseCustomerUsedCreditLimit(Long customerId, BigDecimal usedLoanAmount) {
        log.info("Increasing the used credit limit for Customer with ID: {} by amount: {}", customerId, usedLoanAmount);
        Customer customer = findById(customerId);
        BigDecimal customerCurrentUsedLimit = customer.getUsedCreditLimit();
        BigDecimal customerNewUsedLimit = customerCurrentUsedLimit.add(usedLoanAmount);
        customer.setUsedCreditLimit(customerNewUsedLimit);
        customerRepository.save(customer);
        log.info("Customer with ID: {} has new used credit limit: {}", customerId, customerNewUsedLimit);
    }
}