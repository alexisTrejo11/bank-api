package io.github.alexistrejo11.bank.accounts.domain.model;

import java.util.UUID;

public record OpenedAccount(UUID id, String currency, AccountType type) {
}
