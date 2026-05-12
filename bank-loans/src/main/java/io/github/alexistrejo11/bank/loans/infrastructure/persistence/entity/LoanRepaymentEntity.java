package io.github.alexistrejo11.bank.loans.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import io.github.alexistrejo11.bank.shared.shared_kernel.persistence.UuidJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loan_repayments")
public class LoanRepaymentEntity extends UuidJpaEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "loan_id", nullable = false)
	private LoanEntity loan;

	@Column(name = "installment_number", nullable = false)
	private int installmentNumber;

	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private RepaymentStatus status;

	@Column(name = "paid_at")
	private Instant paidAt;

	protected LoanRepaymentEntity() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private UUID id;
		private LoanEntity loan;
		private int installmentNumber;
		private LocalDate dueDate;
		private BigDecimal amount;
		private RepaymentStatus status;
		private Instant paidAt;

		public Builder id(UUID id) {
			this.id = id;
			return this;
		}

		public Builder loan(LoanEntity loan) {
			this.loan = loan;
			return this;
		}

		public Builder installmentNumber(int installmentNumber) {
			this.installmentNumber = installmentNumber;
			return this;
		}

		public Builder dueDate(LocalDate dueDate) {
			this.dueDate = dueDate;
			return this;
		}

		public Builder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public Builder status(RepaymentStatus status) {
			this.status = status;
			return this;
		}

		public Builder paidAt(Instant paidAt) {
			this.paidAt = paidAt;
			return this;
		}

		public LoanRepaymentEntity build() {
			LoanRepaymentEntity e = new LoanRepaymentEntity();
			e.id = id;
			e.loan = loan;
			e.installmentNumber = installmentNumber;
			e.dueDate = dueDate;
			e.amount = amount;
			e.status = status;
			e.paidAt = paidAt;
			return e;
		}
	}

	public LoanEntity getLoan() {
		return loan;
	}

	public void setLoan(LoanEntity loan) {
		this.loan = loan;
	}

	public int getInstallmentNumber() {
		return installmentNumber;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public RepaymentStatus getStatus() {
		return status;
	}

	public void setStatus(RepaymentStatus status) {
		this.status = status;
	}

	public Instant getPaidAt() {
		return paidAt;
	}

	public void setPaidAt(Instant paidAt) {
		this.paidAt = paidAt;
	}
}
