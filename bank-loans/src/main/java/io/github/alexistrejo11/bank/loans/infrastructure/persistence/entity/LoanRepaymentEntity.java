package io.github.alexistrejo11.bank.loans.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "loan_repayments")
public class LoanRepaymentEntity {

	@Id
	private UUID id;

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

	public LoanRepaymentEntity(
			UUID id,
			int installmentNumber,
			LocalDate dueDate,
			BigDecimal amount,
			RepaymentStatus status,
			Instant paidAt
	) {
		this.id = id;
		this.installmentNumber = installmentNumber;
		this.dueDate = dueDate;
		this.amount = amount;
		this.status = status;
		this.paidAt = paidAt;
	}

	public UUID getId() {
		return id;
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
