package io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.BankAccount;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import io.github.alexistrejo11.bank.accounts.domain.repository.AccountRepository;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.AccountJpaEntity;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.jpa.AccountJpaRepository;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AccountRepositoryRepositoryImpl implements AccountRepository {

	private final AccountJpaRepository jpaRepository;

	public AccountRepositoryRepositoryImpl(AccountJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public void save(BankAccount account) {
		jpaRepository.save(AccountJpaEntity.builder()
				.id(account.id())
				.userId(account.userId())
				.type(account.type())
				.currency(account.currency())
				.status(account.status())
				.createdAt(account.createdAt())
				.updatedAt(account.updatedAt())
				.build());
	}

	@Override
	public Optional<BankAccount> findById(UUID id) {
		return jpaRepository.findById(id)
				.map(this::toDomain);
	}

	@Override
	public Optional<BankAccount> findByIdAndUserId(UUID id, UUID userId) {
		return jpaRepository.findByIdAndUserId(id, userId)
				.map(this::toDomain);
	}

	@Override
	public Optional<OwnedCheckingAccount> findOwnedActiveChecking(UserId userId, UUID accountId) {
		return jpaRepository.findByIdAndUserId(accountId, userId.value())
				.filter(e -> e.getType() == AccountType.CHECKING && e.getStatus() == AccountStatus.ACTIVE)
				.map(e -> new OwnedCheckingAccount(e.getId(), e.getCurrency()));
	}

	private BankAccount toDomain(AccountJpaEntity e) {
		return new BankAccount(
				e.getId(),
				e.getUserId(),
				e.getType(),
				e.getCurrency(),
				e.getStatus(),
				e.getCreatedAt(),
				e.getUpdatedAt(),
				e.getDeletedAt());
	}
}
