package io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntryType;
import io.github.alexistrejo11.bank.shared.shared_kernel.persistence.JpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntryJpaEntity extends JpaEntity {

	@Column(name = "account_id", nullable = false)
	private UUID accountId;

	@Enumerated(EnumType.STRING)
	@Column(name = "entry_type", nullable = false, length = 8)
	private LedgerEntryType entryType;

	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "reference_type", nullable = false)
	private String referenceType;

	@Column(name = "reference_id", nullable = false)
	private UUID referenceId;

	protected LedgerEntryJpaEntity() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private UUID id;
		private UUID accountId;
		private LedgerEntryType entryType;
		private BigDecimal amount;
		private String currency;
		private String referenceType;
		private UUID referenceId;
		private Instant createdAt;

		public Builder id(UUID id) {
			this.id = id;
			return this;
		}

		public Builder accountId(UUID accountId) {
			this.accountId = accountId;
			return this;
		}

		public Builder entryType(LedgerEntryType entryType) {
			this.entryType = entryType;
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

		public Builder referenceType(String referenceType) {
			this.referenceType = referenceType;
			return this;
		}

		public Builder referenceId(UUID referenceId) {
			this.referenceId = referenceId;
			return this;
		}

		public Builder createdAt(Instant createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public LedgerEntryJpaEntity build() {
			LedgerEntryJpaEntity e = new LedgerEntryJpaEntity();
			e.id = id;
			e.accountId = accountId;
			e.entryType = entryType;
			e.amount = amount;
			e.currency = currency;
			e.referenceType = referenceType;
			e.referenceId = referenceId;
			e.createdAt = createdAt;
			e.updatedAt = createdAt;
			return e;
		}
	}

	public UUID getAccountId() {
		return accountId;
	}

	public LedgerEntryType getEntryType() {
		return entryType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public String getReferenceType() {
		return referenceType;
	}

	public UUID getReferenceId() {
		return referenceId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LedgerEntryJpaEntity that = (LedgerEntryJpaEntity) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
