package io.github.alexistrejo11.bank.shared.money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.alexistrejo11.bank.shared.exception.InsufficientFundsException;
import io.github.alexistrejo11.bank.shared.exception.InvalidMoneyAmountException;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

	private static final Currency USD = Currency.getInstance("USD");

	@Test
	@DisplayName("should_reject_non_positive_amount_when_constructing")
	void should_reject_non_positive_amount_when_constructing() {
		assertThatThrownBy(() -> new Money(BigDecimal.ZERO, USD)).isInstanceOf(InvalidMoneyAmountException.class);
		assertThatThrownBy(() -> new Money(new BigDecimal("-1"), USD)).isInstanceOf(InvalidMoneyAmountException.class);
	}

	@Test
	@DisplayName("should_add_when_same_currency")
	void should_add_when_same_currency() {
		Money a = new Money(new BigDecimal("10.00"), USD);
		Money b = new Money(new BigDecimal("5.00"), USD);
		Money sum = a.add(b);
		assertThat(sum.amount()).isEqualByComparingTo(new BigDecimal("15.00"));
		assertThat(sum.currency()).isEqualTo(USD);
	}

	@Test
	@DisplayName("should_subtract_when_result_stays_positive")
	void should_subtract_when_result_stays_positive() {
		Money a = new Money(new BigDecimal("10.00"), USD);
		Money b = new Money(new BigDecimal("3.00"), USD);
		Money diff = a.subtract(b);
		assertThat(diff.amount()).isEqualByComparingTo(new BigDecimal("7.00"));
	}

	@Test
	@DisplayName("should_fail_when_subtract_would_not_remain_positive")
	void should_fail_when_subtract_would_not_remain_positive() {
		Money a = new Money(new BigDecimal("10.00"), USD);
		Money b = new Money(new BigDecimal("10.00"), USD);
		assertThatThrownBy(() -> a.subtract(b)).isInstanceOf(InsufficientFundsException.class);
	}
}
