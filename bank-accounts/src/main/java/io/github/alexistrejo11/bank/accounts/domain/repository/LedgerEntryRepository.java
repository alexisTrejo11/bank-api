package io.github.alexistrejo11.bank.accounts.domain.repository;

import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntry;
import io.github.alexistrejo11.bank.shared.shared_kernel.page.PageResult;
import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerEntryRepository {

	BigDecimal sumBalance(UUID accountId);

	void savePair(LedgerEntry debit, LedgerEntry credit);

	PageResult<LedgerEntry> findPageByAccountId(UUID accountId, int page, int size);
}
