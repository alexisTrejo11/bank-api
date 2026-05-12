package io.github.alexistrejo11.bank.loans.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.loans.domain.model.LoanStatus;
import io.github.alexistrejo11.bank.shared.shared_kernel.persistence.JpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "loans")
public class LoanEntity extends JpaEntity {

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

	@OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<LoanRepaymentEntity> repayments = new ArrayList<>();

	protected LoanEntity() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private UUID id;
		private UUID userId;
		private UUID checkingAccountId;
		private UUID loanAccountId;
		private BigDecimal principal;
		private String currency;
		private BigDecimal monthlyInterestRate;
		private int termMonths;
		private BigDecimal monthlyPayment;
		private LoanStatus status;
		private Instant createdAt;
		private Instant updatedAt;

		public Builder id(UUID id) {
			this.id = id;
			return this;
		}

		public Builder userId(UUID userId) {
			this.userId = userId;
			return this;
		}

		public Builder checkingAccountId(UUID checkingAccountId) {
			this.checkingAccountId = checkingAccountId;
			return this;
		}

		public Builder loanAccountId(UUID loanAccountId) {
			this.loanAccountId = loanAccountId;
			return this;
		}

		public Builder principal(BigDecimal principal) {
			this.principal = principal;
			return this;
		}

		public Builder currency(String currency) {
			this.currency = currency;
			return this;
		}

		public Builder monthlyInterestRate(BigDecimal monthlyInterestRate) {
			this.monthlyInterestRate = monthlyInterestRate;
			return this;
		}

		public Builder termMonths(int termMonths) {
			this.termMonths = termMonths;
			return this;
		}

		public Builder monthlyPayment(BigDecimal monthlyPayment) {
			this.monthlyPayment = monthlyPayment;
			return this;
		}

		public Builder status(LoanStatus status) {
			this.status = status;
			return this;
		}

		public Builder createdAt(Instant createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Builder updatedAt(Instant updatedAt) {
			this.updatedAt = updatedAt;
			return this;
		}

		public LoanEntity build() {
			LoanEntity e = new LoanEntity();
			e.id = id;
			e.userId = userId;
			e.checkingAccountId = checkingAccountId;
			e.loanAccountId = loanAccountId;
			e.principal = principal;
			e.currency = currency;
			e.monthlyInterestRate = monthlyInterestRate;
			e.termMonths = termMonths;
			e.monthlyPayment = monthlyPayment;
			e.status = status;
			e.createdAt = createdAt;
			e.updatedAt = updatedAt;
			return e;
		}
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

	public List<LoanRepaymentEntity> getRepayments() {
		return repayments;
	}
}
