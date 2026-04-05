package io.github.alexistrejo11.bank.accounts.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import io.github.alexistrejo11.bank.accounts.exception.AccountNotFoundException;
import io.github.alexistrejo11.bank.accounts.exception.InvalidTransferException;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.entity.AccountEntity;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.AccountJpaRepository;
import io.github.alexistrejo11.bank.accounts.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LedgerPostingServiceTest {

	@Mock
	private AccountJpaRepository accountRepository;

	@Mock
	private LedgerEntryJpaRepository ledgerEntryRepository;

	@InjectMocks
	private LedgerPostingService ledgerPostingService;

	@Test
	void postTransfer_savesDebitAndCredit() {
		UUID fromId = UUID.randomUUID();
		UUID toId = UUID.randomUUID();
		Instant now = Instant.now();
		AccountEntity from = new AccountEntity(fromId, UUID.randomUUID(), AccountType.CHECKING, "USD", AccountStatus.ACTIVE, now, now);
		AccountEntity to = new AccountEntity(toId, UUID.randomUUID(), AccountType.CHECKING, "USD", AccountStatus.ACTIVE, now, now);
		when(accountRepository.findById(fromId)).thenReturn(Optional.of(from));
		when(accountRepository.findById(toId)).thenReturn(Optional.of(to));

		ledgerPostingService.postTransfer(
				AccountId.of(fromId),
				AccountId.of(toId),
				new BigDecimal("25.50"),
				"usd",
				"TRANSFER",
				UUID.randomUUID()
		);

		verify(ledgerEntryRepository, times(2)).save(any());
	}

	@Test
	void postTransfer_throwsWhenAmountNotPositive() {
		assertThatThrownBy(() -> ledgerPostingService.postTransfer(
				AccountId.random(),
				AccountId.random(),
				BigDecimal.ZERO,
				"USD",
				"TRANSFER",
				UUID.randomUUID()
		)).isInstanceOf(InvalidTransferException.class);
	}

	@Test
	void postTransfer_throwsWhenAccountMissing() {
		UUID fromId = UUID.randomUUID();
		when(accountRepository.findById(fromId)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> ledgerPostingService.postTransfer(
				AccountId.of(fromId),
				AccountId.random(),
				new BigDecimal("1"),
				"USD",
				"TRANSFER",
				UUID.randomUUID()
		)).isInstanceOf(AccountNotFoundException.class);
	}
}
