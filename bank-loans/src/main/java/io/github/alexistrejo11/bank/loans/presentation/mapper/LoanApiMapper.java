package io.github.alexistrejo11.bank.loans.presentation.mapper;

import io.github.alexistrejo11.bank.loans.presentation.dto.response.LoanDetailResponse;
import io.github.alexistrejo11.bank.loans.presentation.dto.response.LoanRepaymentItemResponse;
import io.github.alexistrejo11.bank.loans.presentation.dto.response.PayRepaymentResponse;
import io.github.alexistrejo11.bank.loans.domain.model.LoanAggregate;
import io.github.alexistrejo11.bank.loans.domain.model.LoanRepaymentLine;
import io.github.alexistrejo11.bank.loans.domain.model.RepaymentStatus;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class LoanApiMapper {

	private LoanApiMapper() {
	}

	public static LoanDetailResponse toDetail(LoanAggregate loan) {
		List<LoanRepaymentItemResponse> items = loan.repayments().stream()
				.sorted(Comparator.comparingInt(LoanRepaymentLine::installmentNumber))
				.map(r -> new LoanRepaymentItemResponse(
						r.id(),
						r.installmentNumber(),
						r.dueDate(),
						r.amount(),
						r.status(),
						r.paidAt()
				))
				.toList();
		return new LoanDetailResponse(
				loan.id(),
				loan.checkingAccountId(),
				loan.loanAccountId(),
				loan.principal(),
				loan.currency(),
				loan.monthlyInterestRate(),
				loan.termMonths(),
				loan.monthlyPayment(),
				loan.status(),
				loan.createdAt(),
				items
		);
	}

	public static PayRepaymentResponse toPayRepayment(LoanAggregate loan, UUID repaymentId) {
		return new PayRepaymentResponse(repaymentId, RepaymentStatus.PAID, loan.status());
	}
}
