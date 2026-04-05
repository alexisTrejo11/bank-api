package io.github.alexistrejo11.bank.accounts.domain.query;

import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.util.UUID;

public record GetAccountLedgerQuery(UserId ownerId, UUID accountId, int page, int size) {
}
