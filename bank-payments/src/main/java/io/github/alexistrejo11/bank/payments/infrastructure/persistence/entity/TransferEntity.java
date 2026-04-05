package io.github.alexistrejo11.bank.payments.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.payments.domain.model.TransferStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class TransferEntity {

	@Id
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "source_account_id", nullable = false)
	private UUID sourceAccountId;

	@Column(name = "target_account_id", nullable = false)
	private UUID targetAccountId;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Column(nullable = false, length = 3)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TransferStatus status;

	@Column(name = "idempotency_key", nullable = false)
	private UUID idempotencyKey;

	@Column(name = "failure_reason", length = 1024)
	private String failureReason;

	@Column(name = "reference_transfer_id")
	private UUID referenceTransferId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected TransferEntity() {
	}

	public TransferEntity(
			UUID id,
			UUID userId,
			UUID sourceAccountId,
			UUID targetAccountId,
			BigDecimal amount,
			String currency,
			TransferStatus status,
			UUID idempotencyKey,
			String failureReason,
			UUID referenceTransferId,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = id;
		this.userId = userId;
		this.sourceAccountId = sourceAccountId;
		this.targetAccountId = targetAccountId;
		this.amount = amount;
		this.currency = currency;
		this.status = status;
		this.idempotencyKey = idempotencyKey;
		this.failureReason = failureReason;
		this.referenceTransferId = referenceTransferId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public UUID getSourceAccountId() {
		return sourceAccountId;
	}

	public UUID getTargetAccountId() {
		return targetAccountId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public TransferStatus getStatus() {
		return status;
	}

	public UUID getIdempotencyKey() {
		return idempotencyKey;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public UUID getReferenceTransferId() {
		return referenceTransferId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setStatus(TransferStatus status) {
		this.status = status;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
