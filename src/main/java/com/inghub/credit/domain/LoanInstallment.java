package com.inghub.credit.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "loan_installment")
public class LoanInstallment extends AbstractEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id", referencedColumnName = "id", nullable = false)
    private Loan loan;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paidAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "is_paid", columnDefinition = "TINYINT", nullable = false)
    private boolean paid;

    @Override
    public String toString() {
        return "LoanInstallment{" +
                "id=" + id +
                ", amount=" + amount +
                ", dueDate=" + dueDate +
                ", paidAmount=" + paidAmount +
                ", paid=" + paid +
                ", paymentDate=" + paymentDate +
                '}';
    }
}