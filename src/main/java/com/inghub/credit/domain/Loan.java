package com.inghub.credit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "loan")
public class Loan extends AbstractEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
    private Customer customer;

    @Column(name = "loan_amount", nullable = false)
    private BigDecimal loanAmount;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "number_of_installment", columnDefinition = "SMALLINT", nullable = false)
    private Integer numberOfInstallment;

    @Column(name = "is_paid", columnDefinition = "TINYINT", nullable = false)
    private boolean paid;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoanInstallment> loanInstallments;

    @Override
    public String toString() {
        return "Loan{" +
                "id=" + id +
                ", customer=" + customer +
                ", loanAmount=" + loanAmount +
                ", interestRate=" + interestRate +
                ", numberOfInstallment=" + numberOfInstallment +
                ", paid=" + paid +
                '}';
    }
}