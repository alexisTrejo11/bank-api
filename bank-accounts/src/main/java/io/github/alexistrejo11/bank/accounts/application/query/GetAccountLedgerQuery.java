package io.github.alexistrejo11.bank.accounts.application.query;

import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import java.util.UUID;

public record GetAccountLedgerQuery(UserId ownerId, UUID accountId, int page, int size) {
}
