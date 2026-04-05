package io.github.alexistrejo11.bank.accounts.domain.model;

import java.math.BigDecimal;

public record AccountBalance(BigDecimal amount, String currency) {
}
