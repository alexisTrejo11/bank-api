package io.github.alexistrejo11.bank.accounts.infrastructure.persistence.adapter;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountSummary;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import io.github.alexistrejo11.bank.accounts.domain.port.out.AccountRepository;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.AccountEntity;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AccountRepositoryAdapter implements AccountRepository {

	private final AccountJpaRepository jpaRepository;

	public AccountRepositoryAdapter(AccountJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public void save(UUID id, UUID userId, AccountType type, String currency, AccountStatus status, Instant createdAt, Instant updatedAt) {
		jpaRepository.save(new AccountEntity(id, userId, type, currency, status, createdAt, updatedAt));
	}

	@Override
	public Optional<AccountSummary> findById(UUID id) {
		return jpaRepository.findById(id).map(this::toSummary);
	}

	@Override
	public Optional<AccountSummary> findByIdAndUserId(UUID id, UUID userId) {
		return jpaRepository.findByIdAndUserId(id, userId).map(this::toSummary);
	}

	@Override
	public Optional<OwnedCheckingAccount> findOwnedActiveChecking(UserId userId, UUID accountId) {
		return jpaRepository.findByIdAndUserId(accountId, userId.value())
				.filter(e -> e.getType() == AccountType.CHECKING && e.getStatus() == AccountStatus.ACTIVE)
				.map(e -> new OwnedCheckingAccount(e.getId(), e.getCurrency()));
	}

	private AccountSummary toSummary(AccountEntity e) {
		return new AccountSummary(e.getId(), e.getUserId(), e.getType(), e.getCurrency(), e.getStatus());
	}
}
