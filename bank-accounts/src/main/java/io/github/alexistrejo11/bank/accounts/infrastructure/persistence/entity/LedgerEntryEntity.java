package io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntryEntity {

	@Id
	private UUID id;

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

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected LedgerEntryEntity() {
	}

	public LedgerEntryEntity(
			UUID id,
			UUID accountId,
			LedgerEntryType entryType,
			BigDecimal amount,
			String currency,
			String referenceType,
			UUID referenceId,
			Instant createdAt
	) {
		this.id = id;
		this.accountId = accountId;
		this.entryType = entryType;
		this.amount = amount;
		this.currency = currency;
		this.referenceType = referenceType;
		this.referenceId = referenceId;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
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

	public Instant getCreatedAt() {
		return createdAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LedgerEntryEntity that = (LedgerEntryEntity) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
