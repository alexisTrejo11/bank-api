package io.github.alexistrejo11.bank.accounts.api.dto.response;

import java.math.BigDecimal;

public record BalanceResponse(BigDecimal balance, String currency) {
}
