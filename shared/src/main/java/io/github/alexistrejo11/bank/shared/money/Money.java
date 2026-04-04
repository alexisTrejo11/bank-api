package io.github.alexistrejo11.bank.shared.money;

import io.github.alexistrejo11.bank.shared.exception.InsufficientFundsException;
import io.github.alexistrejo11.bank.shared.exception.InvalidMoneyAmountException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Strictly positive monetary amount in a single currency.
 */
public final class Money {

	private final BigDecimal amount;
	private final Currency currency;

	public Money(BigDecimal amount, Currency currency) {
		if (amount == null) {
			throw new InvalidMoneyAmountException(null);
		}
		if (currency == null) {
			throw new IllegalArgumentException("currency is required");
		}
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InvalidMoneyAmountException(amount);
		}
		this.amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
		this.currency = currency;
	}

	public BigDecimal amount() {
		return amount;
	}

	public Currency currency() {
		return currency;
	}

	public Money add(Money other) {
		requireSameCurrency(other);
		return new Money(this.amount.add(other.amount), currency);
	}

	/**
	 * Subtracts {@code other} from this amount. Result must remain strictly positive.
	 */
	public Money subtract(Money other) {
		requireSameCurrency(other);
		BigDecimal result = this.amount.subtract(other.amount);
		if (result.compareTo(BigDecimal.ZERO) <= 0) {
			throw new InsufficientFundsException("Resulting amount must stay positive");
		}
		return new Money(result, currency);
	}

	private void requireSameCurrency(Money other) {
		if (!this.currency.equals(other.currency)) {
			throw new IllegalArgumentException("Currency mismatch");
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Money money = (Money) o;
		return amount.equals(money.amount) && currency.equals(money.currency);
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, currency);
	}

	@Override
	public String toString() {
		return amount + " " + currency.getCurrencyCode();
	}
}
