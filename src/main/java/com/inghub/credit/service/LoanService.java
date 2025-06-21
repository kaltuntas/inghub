package com.inghub.credit.service;

import com.inghub.credit.util.ConstantValues;
import com.inghub.credit.domain.Customer;
import com.inghub.credit.domain.Loan;
import com.inghub.credit.domain.LoanInstallment;
import com.inghub.credit.exception.ResourceNotFoundException;
import com.inghub.credit.repository.LoanRepository;
import com.inghub.credit.repository.spec.LoanSpecification;
import com.inghub.credit.repository.page.PageRequestBuilder;
import com.inghub.credit.model.ApiModelPage;
import com.inghub.credit.model.CreateLoanResponse;
import com.inghub.credit.model.ListLoanResponse;
import com.inghub.credit.model.dto.LoanDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final CustomerService customerService;
    private final LoanInstallmentService loanInstallmentService;

    public Loan findById(Long id) {
        log.info("Attempting to find Loan with ID: {}", id);
        Optional<Loan> loan = loanRepository.findById(id);
        if (loan.isEmpty()) {
            log.error("Loan not found with given id: {}", id);
            throw new ResourceNotFoundException("Loan not found with given id: " + id);
        } else {
            log.info("Found Loan with ID: {}", id);
            return loan.get();
        }
    }

    public void updateLoanIsPaidStatus(Long loanId, boolean paid) {
        log.info("Updating Loan with ID: {} to paid status: {}", loanId, paid);
        Loan loan = findById(loanId);
        loan.setPaid(paid);
        loanRepository.save(loan);
        log.info("Loan with ID: {} updated to paid status: {}", loanId, paid);
    }

    public ListLoanResponse searchLoansByCustomerId(Long customerId, BigDecimal loanAmount, Integer installmentCount, Boolean paid, Integer pageSize, Integer pageNumber, String sort) {
        log.info("Searching loans for customerId: {}, loanAmount: {}, installmentCount: {}, paid: {}, pageSize: {}, pageNumber: {}, sort: {}",
                 customerId, loanAmount, installmentCount, paid, pageSize, pageNumber, sort);

        //TODO: check whether the customer exists with given customerId and throw exception?

        log.debug("Building page request with pageSize: {}, pageNumber: {}, sort: {}", pageSize, pageNumber, sort);
        PageRequest pageRequest = PageRequestBuilder.getPageRequest(pageSize, pageNumber, sort);
        Specification<Loan> spec = LoanSpecification.getSpecificationForLoan(customerId, loanAmount, installmentCount, paid);
        Page<Loan> loansPage = loanRepository.findAll(spec, pageRequest);
        List<Loan> loans = loansPage.getContent();

        ApiModelPage pagingResponse = new ApiModelPage(pageRequest.getPageNumber() + 1, pageRequest.getPageSize(),
                                                       loansPage.getTotalElements(), loansPage.getTotalPages(),
                                                       loansPage.hasNext(), loansPage.hasPrevious());

        log.info("Returning {} loans for the search criteria", loans.size());
        return new ListLoanResponse(loans.stream().map(this::mapLoanEntityToDTO).collect(Collectors.toList()), pagingResponse);
    }

    @Transactional
    public CreateLoanResponse createLoan(Long customerId, BigDecimal loanAmount, int numberOfInstallment, BigDecimal interestRate) {
        log.info("Creating loan for Customer ID: {} with loanAmount: {}, numberOfInstallments: {}, interestRate: {}", customerId, loanAmount, numberOfInstallment, interestRate);
        Loan loan = saveLoan(customerId, loanAmount, numberOfInstallment, interestRate);
        log.info("Loan created with ID: {}", loan.getId());
        return new CreateLoanResponse(loan.getId(), loan.getCreateDate(), customerId, loanAmount.doubleValue(), numberOfInstallment);
    }

    @Transactional
    public Loan saveLoan(long customerId, BigDecimal loanAmount, int numberOfInstallment, BigDecimal interestRate) {
        log.info("Saving loan for Customer ID: {} with loanAmount: {}, numberOfInstallments: {}, interestRate: {}", customerId, loanAmount, numberOfInstallment, interestRate);
        customerService.checkCustomerHasEnoughLimitToGetNewLoan(customerId, loanAmount);
        LoanInstallmentService.checkNumberOfInstallmentIsValid(numberOfInstallment);
        checkInterestRateIsValid(interestRate);

        Customer customer = customerService.findById(customerId);
        customerService.increaseCustomerUsedCreditLimit(customerId, loanAmount);

        Loan loan = new Loan();
        List<LocalDate> installmentDates = LoanInstallmentService.createInstallmentDatesByInstallmentCount(numberOfInstallment);
        List<LoanInstallment> installments = loanInstallmentService.createLoanInstallments(loan, loanAmount, numberOfInstallment, installmentDates, interestRate);

        loan.setCustomer(customer);
        loan.setInterestRate(interestRate);
        loan.setLoanAmount(loanAmount);
        loan.setNumberOfInstallment(numberOfInstallment);
        loan.setLoanInstallments(installments);
        log.info("Loan saved with ID: {}", loan.getId());
        return loanRepository.save(loan);
    }

    public static void checkInterestRateIsValid(BigDecimal interestRate) {
        log.debug("Checking if interest rate {} is valid.", interestRate);
        if (interestRate.compareTo(ConstantValues.VALID_INTEREST_RATE_RANGE_MIN) < 0 || interestRate.compareTo(ConstantValues.VALID_INTEREST_RATE_RANGE_MAX) > 0) {
            log.error("Invalid interest rate: {}. Valid range is between {} and {}", interestRate, ConstantValues.VALID_INTEREST_RATE_RANGE_MIN, ConstantValues.VALID_INTEREST_RATE_RANGE_MAX);
            throw new IllegalArgumentException("Interest rate must be between " + ConstantValues.VALID_INTEREST_RATE_RANGE_MIN + " and " + ConstantValues.VALID_INTEREST_RATE_RANGE_MAX);
        }
        log.debug("Interest rate {} is valid.", interestRate);
    }

    private LoanDTO mapLoanEntityToDTO(Loan loan) {
        log.debug("Mapping Loan entity to DTO for Loan ID: {}", loan.getId());
        return new LoanDTO(loan.getId(), loan.getCreateDate(), loan.getUpdateDate(),
                           loan.getCustomer().getId(), loan.getLoanAmount().doubleValue(), loan.getNumberOfInstallment(),
                           loan.isPaid());
    }
}