package io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import io.github.alexistrejo11.bank.shared.shared_kernel.persistence.JpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity extends JpaEntity {

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccountType type;

	@Column(nullable = false, length = 3)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AccountStatus status;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	protected AccountJpaEntity() {
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private UUID id;
		private UUID userId;
		private AccountType type;
		private String currency;
		private AccountStatus status;
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

		public Builder type(AccountType type) {
			this.type = type;
			return this;
		}

		public Builder currency(String currency) {
			this.currency = currency;
			return this;
		}

		public Builder status(AccountStatus status) {
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

		public AccountJpaEntity build() {
			AccountJpaEntity e = new AccountJpaEntity();
			e.id = id;
			e.userId = userId;
			e.type = type;
			e.currency = currency;
			e.status = status;
			e.createdAt = createdAt;
			e.updatedAt = updatedAt;
			return e;
		}
	}

	public UUID getUserId() {
		return userId;
	}

	public AccountType getType() {
		return type;
	}

	public String getCurrency() {
		return currency;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AccountJpaEntity that = (AccountJpaEntity) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
