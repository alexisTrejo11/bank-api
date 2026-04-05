package io.github.alexistrejo11.bank.accounts.api.mapper;

import io.github.alexistrejo11.bank.accounts.api.dto.response.BalanceResponse;
import io.github.alexistrejo11.bank.accounts.api.dto.response.LedgerEntryResponse;
import io.github.alexistrejo11.bank.accounts.api.dto.response.LedgerPageResponse;
import io.github.alexistrejo11.bank.accounts.api.dto.response.OpenAccountResponse;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountBalance;
import io.github.alexistrejo11.bank.accounts.domain.model.LedgerEntry;
import io.github.alexistrejo11.bank.accounts.domain.model.OpenedAccount;
import io.github.alexistrejo11.bank.shared.page.PageResult;
import java.util.List;

public final class AccountApiMapper {

	private AccountApiMapper() {
	}

	public static OpenAccountResponse toOpenResponse(OpenedAccount opened) {
		return new OpenAccountResponse(opened.id(), opened.currency(), opened.type());
	}

	public static BalanceResponse toBalanceResponse(AccountBalance balance) {
		return new BalanceResponse(balance.amount(), balance.currency());
	}

	public static LedgerPageResponse toLedgerPageResponse(PageResult<LedgerEntry> page) {
		List<LedgerEntryResponse> content = page.content().stream().map(AccountApiMapper::toLedgerEntryResponse).toList();
		return new LedgerPageResponse(content, page.totalElements(), page.page(), page.size());
	}

	private static LedgerEntryResponse toLedgerEntryResponse(LedgerEntry e) {
		return new LedgerEntryResponse(
				e.id(),
				e.entryType(),
				e.amount(),
				e.currency(),
				e.referenceType(),
				e.referenceId(),
				e.createdAt()
		);
	}
}
