package io.github.alexistrejo11.bank.loans.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class AmortizationCalculator {

	private AmortizationCalculator() {
	}

	/**
	 * Fixed monthly payment: M = P * r * (1+r)^n / ((1+r)^n - 1). {@code monthlyRate} is decimal per month (e.g. 0.01 = 1%).
	 */
	public static BigDecimal monthlyPayment(BigDecimal principal, BigDecimal monthlyRate, int termMonths) {
		if (termMonths <= 0) {
			throw new IllegalArgumentException("termMonths must be positive");
		}
		if (principal.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("principal must be positive");
		}
		if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
			return principal.divide(BigDecimal.valueOf(termMonths), 4, RoundingMode.HALF_UP);
		}
		BigDecimal one = BigDecimal.ONE;
		BigDecimal onePlusR = one.add(monthlyRate);
		BigDecimal pow = intPow(onePlusR, termMonths);
		BigDecimal numerator = principal.multiply(monthlyRate).multiply(pow);
		BigDecimal denominator = pow.subtract(one);
		return numerator.divide(denominator, 4, RoundingMode.HALF_UP);
	}

	public static List<InstallmentDraft> buildSchedule(LocalDate firstDue, BigDecimal principal, BigDecimal monthlyRate, int termMonths) {
		BigDecimal balance = principal.setScale(4, RoundingMode.HALF_UP);
		BigDecimal fixedPayment = monthlyPayment(principal, monthlyRate, termMonths);
		List<InstallmentDraft> rows = new ArrayList<>();
		for (int m = 1; m <= termMonths; m++) {
			LocalDate due = firstDue.plusMonths(m - 1L);
			BigDecimal interest = balance.multiply(monthlyRate).setScale(4, RoundingMode.HALF_UP);
			BigDecimal principalPart;
			BigDecimal payment;
			if (m == termMonths) {
				principalPart = balance;
				payment = principalPart.add(interest).setScale(4, RoundingMode.HALF_UP);
			}
			else {
				payment = fixedPayment;
				principalPart = payment.subtract(interest);
				if (principalPart.compareTo(balance) > 0) {
					principalPart = balance;
					payment = principalPart.add(interest).setScale(4, RoundingMode.HALF_UP);
				}
			}
			rows.add(new InstallmentDraft(m, due, payment));
			balance = balance.subtract(principalPart).setScale(4, RoundingMode.HALF_UP);
		}
		return rows;
	}

	private static BigDecimal intPow(BigDecimal base, int exp) {
		BigDecimal r = BigDecimal.ONE;
		for (int i = 0; i < exp; i++) {
			r = r.multiply(base);
		}
		return r;
	}

	public record InstallmentDraft(int installmentNumber, LocalDate dueDate, BigDecimal amount) {
	}
}
