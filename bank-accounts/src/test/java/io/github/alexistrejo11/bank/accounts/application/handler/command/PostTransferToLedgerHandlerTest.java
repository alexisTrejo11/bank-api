package io.github.alexistrejo11.bank.accounts.application.handler.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.alexistrejo11.bank.accounts.application.command.PostTransferToLedgerCommand;
import io.github.alexistrejo11.bank.accounts.domain.exception.AccountNotFoundException;
import io.github.alexistrejo11.bank.accounts.domain.exception.InvalidTransferException;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountStatus;
import io.github.alexistrejo11.bank.accounts.domain.model.BankAccount;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import java.time.Instant;
import io.github.alexistrejo11.bank.accounts.domain.repository.AccountRepository;
import io.github.alexistrejo11.bank.accounts.domain.repository.LedgerEntryRepository;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.AccountId;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostTransferToLedgerHandlerTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private LedgerEntryRepository ledgerEntryRepository;

	@InjectMocks
	private PostTransferToLedgerHandler handler;

	@Test
	void execute_savesDebitAndCredit() {
		UUID fromId = UUID.randomUUID();
		UUID toId = UUID.randomUUID();
		Instant now = Instant.now();
		BankAccount from = new BankAccount(
				fromId, UUID.randomUUID(), AccountType.CHECKING, "USD", AccountStatus.ACTIVE, now, now, null);
		BankAccount to = new BankAccount(
				toId, UUID.randomUUID(), AccountType.CHECKING, "USD", AccountStatus.ACTIVE, now, now, null);
		when(accountRepository.findById(fromId)).thenReturn(Optional.of(from));
		when(accountRepository.findById(toId)).thenReturn(Optional.of(to));

		handler.execute(new PostTransferToLedgerCommand(
				AccountId.of(fromId),
				AccountId.of(toId),
				new BigDecimal("25.50"),
				"usd",
				"TRANSFER",
				UUID.randomUUID()));

		verify(ledgerEntryRepository, times(1)).savePair(any(), any());
	}

	@Test
	void execute_throwsWhenAmountNotPositive() {
		assertThatThrownBy(() -> handler.execute(new PostTransferToLedgerCommand(
				AccountId.random(),
				AccountId.random(),
				BigDecimal.ZERO,
				"USD",
				"TRANSFER",
				UUID.randomUUID()))).isInstanceOf(InvalidTransferException.class);
	}

	@Test
	void execute_throwsWhenAccountMissing() {
		UUID fromId = UUID.randomUUID();
		when(accountRepository.findById(fromId)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> handler.execute(new PostTransferToLedgerCommand(
				AccountId.of(fromId),
				AccountId.random(),
				new BigDecimal("1"),
				"USD",
				"TRANSFER",
				UUID.randomUUID()))).isInstanceOf(AccountNotFoundException.class);
	}
}
