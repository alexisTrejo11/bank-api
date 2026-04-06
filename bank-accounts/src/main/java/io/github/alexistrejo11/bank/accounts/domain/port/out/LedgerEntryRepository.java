package io.github.alexistrejo11.bank.accounts.domain.port.out;

import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntry;
import io.github.alexistrejo11.bank.shared.page.PageResult;
import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerEntryRepository {

	BigDecimal sumBalance(UUID accountId);

	void savePair(LedgerEntry debit, LedgerEntry credit);

	PageResult<LedgerEntry> findPageByAccountId(UUID accountId, int page, int size);
}
