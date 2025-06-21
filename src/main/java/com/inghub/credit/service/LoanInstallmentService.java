package com.inghub.credit.service;

import com.inghub.credit.util.ConstantValues;
import com.inghub.credit.domain.Loan;
import com.inghub.credit.domain.LoanInstallment;
import com.inghub.credit.exception.ResourceNotFoundException;
import com.inghub.credit.repository.LoanInstallmentRepository;
import com.inghub.credit.repository.page.PageRequestBuilder;
import com.inghub.credit.model.ApiModelPage;
import com.inghub.credit.model.ListLoanInstallmentsResponse;
import com.inghub.credit.model.dto.LoanInstallmentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoanInstallmentService {

    private final LoanInstallmentRepository loanInstallmentRepository;

    public LoanInstallment findById(Long id) {
        log.info("Attempting to find LoanInstallment with ID: {}", id);
        Optional<LoanInstallment> loanInstallment = loanInstallmentRepository.findById(id);
        if (loanInstallment.isEmpty()) {
            log.error("LoanInstallment not found with given id: {}", id);
            throw new ResourceNotFoundException("LoanInstallment not found with given id: " + id);
        } else {
            log.info("Found LoanInstallment with ID: {}", id);
            return loanInstallment.get();
        }
    }

    public Page<LoanInstallment> getPaginatedLoanInstallmentsByLoanId(Long loanId, PageRequest pageRequest) {
        log.info("Fetching paginated LoanInstallments for Loan ID: {}", loanId);
        return loanInstallmentRepository.findByLoanId(loanId, pageRequest);
    }

    public List<LoanInstallment> findLoanInstallmentsByLoanIdAndIsPaid(Long loanId, boolean paid) {
        log.info("Fetching LoanInstallments for Loan ID: {} with paid status: {}", loanId, paid);
        return loanInstallmentRepository.findByLoanIdAndPaidOrderByDueDate(loanId, paid);
    }

    public ListLoanInstallmentsResponse searchLoanInstallmentsByLoanId(Long loanId, Integer pageSize, Integer pageNumber, String sort) {
        log.info("Searching LoanInstallments for Loan ID: {} with pageSize: {}, pageNumber: {}, sort: {}", loanId, pageSize, pageNumber, sort);

        //TODO: check whether the loan exists with given loanId and throw exception?

        PageRequest pageRequest = PageRequestBuilder.getPageRequest(pageSize, pageNumber, sort);
        Page<LoanInstallment> loanInstallmentsPage = getPaginatedLoanInstallmentsByLoanId(loanId, pageRequest);
        List<LoanInstallment> loanInstallments = loanInstallmentsPage.getContent();

        ApiModelPage pagingResponse = new ApiModelPage(pageRequest.getPageNumber() + 1, pageRequest.getPageSize(),
                                                       loanInstallmentsPage.getTotalElements(), loanInstallmentsPage.getTotalPages(),
                                                       loanInstallmentsPage.hasNext(), loanInstallmentsPage.hasPrevious());
        log.info("LoanInstallments search result for Loan ID: {} returned {} installments.", loanId, loanInstallments.size());
        return new ListLoanInstallmentsResponse(loanId, loanInstallments.stream().map(this::mapLoanInstallmentEntityToDTO).collect(Collectors.toList()), pagingResponse);
    }

    public static List<LocalDate> createInstallmentDatesByInstallmentCount(int numberOfInstallment) {
        log.info("Creating installment dates for {} installments.", numberOfInstallment);
        List<LocalDate> installmentDates = new ArrayList<>(numberOfInstallment);
        LocalDate findFirstDayOfNextMonth = findFirstDayOfNextMonth();
        for (int i = 0; i < numberOfInstallment; i++) {
            LocalDate date = findFirstDayOfNextMonth.plusMonths(i);
            installmentDates.add(date);
        }

        return installmentDates;
    }

    public static LocalDate findFirstDayOfNextMonth() {
        LocalDate today = LocalDate.now();
        return today.withDayOfMonth(1).plusMonths(1);
    }

    public static BigDecimal calculateTotalAmountToBePaid(BigDecimal loanAmount, int numberOfInstallment, BigDecimal interestRate) {
        BigDecimal interestRatio = BigDecimal.ONE.add(interestRate);
        return loanAmount.multiply(interestRatio);
    }

    public static BigDecimal calculateInstallmentAmount(BigDecimal totalAmountToBePaid, int numberOfInstallment) {
        return totalAmountToBePaid.divide(new BigDecimal(numberOfInstallment), 2, RoundingMode.HALF_UP);
    }

    public List<LoanInstallment> createLoanInstallments(Loan loan, BigDecimal loanAmount, int numberOfInstallment, List<LocalDate> installmentDates, BigDecimal interestRate) {
        log.info("Creating LoanInstallments for Loan ID: {} with loanAmount: {}, numberOfInstallments: {}, interestRate: {}", loan.getId(), loanAmount, numberOfInstallment, interestRate);

        BigDecimal totalAmountToBePaid = calculateTotalAmountToBePaid(loanAmount, numberOfInstallment, interestRate);
        BigDecimal installmentAmount = calculateInstallmentAmount(totalAmountToBePaid, numberOfInstallment);

        List<LoanInstallment> loanInstallments = new ArrayList<>(numberOfInstallment);
        for (LocalDate installmentDate : installmentDates) {
            LoanInstallment loanInstallment = new LoanInstallment();
            loanInstallment.setLoan(loan);
            loanInstallment.setAmount(installmentAmount);
            loanInstallment.setPaidAmount(BigDecimal.ZERO);
            loanInstallment.setDueDate(installmentDate);
            loanInstallment.setPaid(false);
            loanInstallments.add(loanInstallment);
        }

        checkTotalInstallmentAmountsValid(loanInstallments, totalAmountToBePaid, installmentAmount);
        log.info("Created {} LoanInstallments for Loan ID: {}", loanInstallments.size(), loan.getId());

        return loanInstallments;
    }

    public void payMultipleLoanInstallments(List<Long> loanInstallmentIds) {
        log.info("Processing payments for multiple LoanInstallments: {}", loanInstallmentIds);
        for (Long id : loanInstallmentIds) {
            LoanInstallment loanInstallment = findById(id);
            loanInstallment.setPaidAmount(loanInstallment.getAmount());
            loanInstallment.setPaymentDate(LocalDateTime.now());
            loanInstallment.setPaid(true);
            loanInstallmentRepository.save(loanInstallment);
            log.info("LoanInstallment with ID: {} marked as paid.", id);
        }
    }

    //Installments have due date that still more than 3 calendar months cannot be paid.
    public static boolean checkInstallmentHaveDueDateMoreThanGivenDurationInMonths(LocalDate installmentDueDate, int months) {
        log.info("Checking if LoanInstallment with due date {} is more than {} months from now.", installmentDueDate, months);
        LocalDate currentDate = LocalDate.now();
        LocalDate dateAfterGivenMonthsFromNow = currentDate.plusMonths(months);
        boolean result = installmentDueDate.isAfter(dateAfterGivenMonthsFromNow);
        log.info("LoanInstallment with due date {} is {} months from now.", installmentDueDate, result ? "more" : "not more");
        return result;
    }

    public List<LoanInstallment> findEligibleInstallments(List<LoanInstallment> loanInstallmentList, BigDecimal paidAmount) {
        log.debug("Finding installments that can be paid with amount: {}", paidAmount);

        Queue<LoanInstallment> installPaymentQueue = new LinkedList<>(loanInstallmentList);
        List<LoanInstallment> ableToPayInstallments = new ArrayList<>();

        while (!installPaymentQueue.isEmpty() && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            LoanInstallment nextInstallment = installPaymentQueue.peek(); // Get the earliest installment.
            log.debug("Evaluating installment with ID: {}", nextInstallment.getId());

            //Installments have due date that still more than 3 calendar months cannot be paid.
            boolean installmentDueDateIsAfter3Months = checkInstallmentHaveDueDateMoreThanGivenDurationInMonths(nextInstallment.getDueDate(), 3);
            if (installmentDueDateIsAfter3Months) {
                log.info("Stopping payments. Installment with ID {} has a due date more than 3 months away.", nextInstallment.getId());
                break;
            }

            if (paidAmount.compareTo(nextInstallment.getAmount()) >= 0) {
                log.debug("Adding installment with ID: {} to payment list", nextInstallment.getId());
                paidAmount = paidAmount.subtract(nextInstallment.getAmount()); // Deduct the installment amount from the payment.
                ableToPayInstallments.add(nextInstallment);
                installPaymentQueue.poll(); // Remove the paid installment and to list.
            } else {
                log.info("Insufficient funds to pay installment with ID: {}", nextInstallment.getId());
                break; // Stop if the amount is insufficient to pay the next installment.
            }
        }
        log.info("Total installments count that can be paid: {}", ableToPayInstallments.size());
        return ableToPayInstallments;
    }

    public static void checkNumberOfInstallmentIsValid(int numberOfInstallment) {
        log.debug("Checking if number of installments {} is valid.", numberOfInstallment);
        if (!ConstantValues.VALID_INSTALLMENT_NUMBERS.contains(numberOfInstallment)) {
            log.error("Invalid number of installments: {}. Valid numbers are: {}", numberOfInstallment, ConstantValues.VALID_INSTALLMENT_NUMBERS);
            throw new IllegalArgumentException("Invalid number of installments. Must be: " + ConstantValues.VALID_INSTALLMENT_NUMBERS);
        }
        log.debug("Number of installments {} is valid.", numberOfInstallment);
    }


    private LoanInstallmentDTO mapLoanInstallmentEntityToDTO(LoanInstallment loanInstallment) {
        log.debug("Mapping LoanInstallment entity to DTO for LoanInstallment ID: {}", loanInstallment.getId());
        return new LoanInstallmentDTO(loanInstallment.getId(), loanInstallment.getCreateDate(), loanInstallment.getUpdateDate(),
                                      loanInstallment.getAmount().doubleValue(), loanInstallment.getPaidAmount().doubleValue(), loanInstallment.getDueDate(),
                                      loanInstallment.getPaymentDate(), loanInstallment.isPaid());
    }

    private void checkTotalInstallmentAmountsValid(List<LoanInstallment> installmentList, BigDecimal totalAmountToBePaid, BigDecimal installmentAmount) {

        BigDecimal totalInstallmentAmount = installmentAmount.multiply(new BigDecimal(installmentList.size()));
        BigDecimal difference = totalInstallmentAmount.subtract(totalAmountToBePaid);

        if(difference.compareTo(BigDecimal.ZERO) > 0) {
            installmentList.get(installmentList.size()-1).setAmount(installmentAmount.add(difference));
        }
        else {
            installmentList.get(installmentList.size()-1).setAmount(installmentAmount.subtract(difference));
        }

    }

}