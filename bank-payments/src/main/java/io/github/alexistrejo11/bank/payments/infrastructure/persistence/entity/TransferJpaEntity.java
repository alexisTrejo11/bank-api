package io.github.alexistrejo11.bank.payments.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.payments.domain.model.TransferStatus;
import io.github.alexistrejo11.bank.shared.shared_kernel.persistence.JpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class TransferJpaEntity extends JpaEntity {

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

	protected TransferJpaEntity() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private UUID id;
		private UUID userId;
		private UUID sourceAccountId;
		private UUID targetAccountId;
		private BigDecimal amount;
		private String currency;
		private TransferStatus status;
		private UUID idempotencyKey;
		private String failureReason;
		private UUID referenceTransferId;
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

		public Builder sourceAccountId(UUID sourceAccountId) {
			this.sourceAccountId = sourceAccountId;
			return this;
		}

		public Builder targetAccountId(UUID targetAccountId) {
			this.targetAccountId = targetAccountId;
			return this;
		}

		public Builder amount(BigDecimal amount) {
			this.amount = amount;
			return this;
		}

		public Builder currency(String currency) {
			this.currency = currency;
			return this;
		}

		public Builder status(TransferStatus status) {
			this.status = status;
			return this;
		}

		public Builder idempotencyKey(UUID idempotencyKey) {
			this.idempotencyKey = idempotencyKey;
			return this;
		}

		public Builder failureReason(String failureReason) {
			this.failureReason = failureReason;
			return this;
		}

		public Builder referenceTransferId(UUID referenceTransferId) {
			this.referenceTransferId = referenceTransferId;
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

		public TransferJpaEntity build() {
			TransferJpaEntity entity = new TransferJpaEntity();
			entity.id = id;
			entity.userId = userId;
			entity.sourceAccountId = sourceAccountId;
			entity.targetAccountId = targetAccountId;
			entity.amount = amount;
			entity.currency = currency;
			entity.status = status;
			entity.idempotencyKey = idempotencyKey;
			entity.failureReason = failureReason;
			entity.referenceTransferId = referenceTransferId;
			entity.createdAt = createdAt;
			entity.updatedAt = updatedAt;
			return entity;
		}
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

	public void setStatus(TransferStatus status) {
		this.status = status;
	}

	public void setFailureReason(String failureReason) {
		this.failureReason = failureReason;
	}
}
