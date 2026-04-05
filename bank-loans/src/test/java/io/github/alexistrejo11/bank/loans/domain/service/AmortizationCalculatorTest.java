package io.github.alexistrejo11.bank.loans.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AmortizationCalculatorTest {

	@Test
	@DisplayName("monthlyPayment matches standard amortization formula for non-zero rate")
	void monthly_payment_formula() {
		BigDecimal p = new BigDecimal("10000");
		BigDecimal r = new BigDecimal("0.01");
		int n = 12;
		BigDecimal m = AmortizationCalculator.monthlyPayment(p, r, n);
		assertThat(m).isEqualByComparingTo(new BigDecimal("888.4879"));
	}

	@Test
	@DisplayName("zero interest splits principal evenly across months")
	void zero_rate_equal_installments() {
		BigDecimal p = new BigDecimal("1200");
		BigDecimal m = AmortizationCalculator.monthlyPayment(p, BigDecimal.ZERO, 12);
		assertThat(m).isEqualByComparingTo(new BigDecimal("100.0000"));
	}

	@Test
	@DisplayName("schedule principal portions sum to original principal")
	void schedule_covers_principal() {
		LocalDate first = LocalDate.of(2026, 1, 1);
		BigDecimal principal = new BigDecimal("6000");
		BigDecimal rate = new BigDecimal("0.005");
		int term = 24;
		var rows = AmortizationCalculator.buildSchedule(first, principal, rate, term);
		assertThat(rows).hasSize(term);
		BigDecimal sumPrincipal = BigDecimal.ZERO;
		BigDecimal balance = principal.setScale(4, RoundingMode.HALF_UP);
		for (var row : rows) {
			BigDecimal interest = balance.multiply(rate).setScale(4, RoundingMode.HALF_UP);
			BigDecimal principalPart = row.amount().subtract(interest);
			sumPrincipal = sumPrincipal.add(principalPart);
			balance = balance.subtract(principalPart).setScale(4, RoundingMode.HALF_UP);
		}
		assertThat(sumPrincipal).isEqualByComparingTo(principal.setScale(4, RoundingMode.HALF_UP));
		assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
	}

	@Test
	@DisplayName("rejects non-positive term")
	void rejects_bad_term() {
		assertThatThrownBy(() -> AmortizationCalculator.monthlyPayment(new BigDecimal("100"), new BigDecimal("0.01"), 0))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
