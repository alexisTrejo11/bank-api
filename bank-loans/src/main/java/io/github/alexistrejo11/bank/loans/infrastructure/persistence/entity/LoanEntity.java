package io.github.alexistrejo11.bank.loans.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "loans")
public class LoanEntity {

	@Id
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "checking_account_id", nullable = false)
	private UUID checkingAccountId;

	@Column(name = "loan_account_id")
	private UUID loanAccountId;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal principal;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "monthly_interest_rate", nullable = false, precision = 19, scale = 8)
	private BigDecimal monthlyInterestRate;

	@Column(name = "term_months", nullable = false)
	private int termMonths;

	@Column(name = "monthly_payment", nullable = false, precision = 19, scale = 4)
	private BigDecimal monthlyPayment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private LoanStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LoanRepaymentEntity> repayments = new ArrayList<>();

	protected LoanEntity() {
	}

	public LoanEntity(
			UUID id,
			UUID userId,
			UUID checkingAccountId,
			UUID loanAccountId,
			BigDecimal principal,
			String currency,
			BigDecimal monthlyInterestRate,
			int termMonths,
			BigDecimal monthlyPayment,
			LoanStatus status,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.userId = userId;
		this.checkingAccountId = checkingAccountId;
		this.loanAccountId = loanAccountId;
		this.principal = principal;
		this.currency = currency;
		this.monthlyInterestRate = monthlyInterestRate;
		this.termMonths = termMonths;
		this.monthlyPayment = monthlyPayment;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public UUID getCheckingAccountId() {
		return checkingAccountId;
	}

	public UUID getLoanAccountId() {
		return loanAccountId;
	}

	public void setLoanAccountId(UUID loanAccountId) {
		this.loanAccountId = loanAccountId;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public String getCurrency() {
		return currency;
	}

	public BigDecimal getMonthlyInterestRate() {
		return monthlyInterestRate;
	}

	public int getTermMonths() {
		return termMonths;
	}

	public BigDecimal getMonthlyPayment() {
		return monthlyPayment;
	}

	public LoanStatus getStatus() {
		return status;
	}

	public void setStatus(LoanStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<LoanRepaymentEntity> getRepayments() {
		return repayments;
	}
}
