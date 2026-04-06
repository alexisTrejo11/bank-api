package io.github.alexistrejo11.bank.openapi;

import io.github.alexistrejo11.bank.shared.openapi.BankApiKeys;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * All operation-level OpenAPI copy lives here (and in DTO {@code @Schema} annotations), not on controllers.
 */
public final class BankApiDocumentationCatalog {

	private BankApiDocumentationCatalog() {
	}

	public static Map<String, BankApiOperationDoc> all() {
		Map<String, BankApiOperationDoc> m = new LinkedHashMap<>();
		auth(m);
		accounts(m);
		payments(m);
		loans(m);
		audit(m);
		notifications(m);
		return Map.copyOf(m);
	}

	private static void auth(Map<String, BankApiOperationDoc> m) {
		m.put(BankApiKeys.AUTH_REGISTER, new BankApiOperationDoc(
				"Register a new customer",
				"Creates a user with the CUSTOMER role, persists credentials (bcrypt), and returns access + refresh tokens (RS256 JWT).",
				"Authentication",
				false));
		m.put(BankApiKeys.AUTH_LOGIN, new BankApiOperationDoc(
				"Authenticate and obtain tokens",
				"Validates email/password and returns a short-lived access token and refresh token. Use Authorization: Bearer &lt;access&gt; on subsequent calls.",
				"Authentication",
				false));
		m.put(BankApiKeys.AUTH_REFRESH, new BankApiOperationDoc(
				"Refresh access token",
				"Rotates the refresh token and returns a new access token. The previous refresh token is invalidated.",
				"Authentication",
				false));
		m.put(BankApiKeys.AUTH_LOGOUT, new BankApiOperationDoc(
				"Logout (blocklist access token)",
				"Adds the current JWT id (jti) to the Redis blocklist so the access token cannot be reused before expiry.",
				"Authentication"));
		m.put(BankApiKeys.AUTH_ME, new BankApiOperationDoc(
				"Current user profile",
				"Returns the authenticated user id and email from the JWT.",
				"Authentication"));
	}

	private static void accounts(Map<String, BankApiOperationDoc> m) {
		m.put(BankApiKeys.ACCOUNTS_OPEN, new BankApiOperationDoc(
				"Open a new account",
				"Creates a CHECKING or SAVINGS account in the given ISO currency for the authenticated user.",
				"Accounts"));
		m.put(BankApiKeys.ACCOUNTS_BALANCE, new BankApiOperationDoc(
				"Get derived account balance",
				"Computes balance from double-entry ledger (sum of credits minus debits). Requires ownership or admin.",
				"Accounts"));
		m.put(BankApiKeys.ACCOUNTS_LEDGER, new BankApiOperationDoc(
				"List ledger entries",
				"Paginated ledger lines for the account, newest first.",
				"Accounts"));
	}

	private static void payments(Map<String, BankApiOperationDoc> m) {
		m.put(BankApiKeys.PAYMENTS_TRANSFER, new BankApiOperationDoc(
				"Initiate transfer",
				"Moves funds between accounts with Redis-backed idempotency (Idempotency-Key header). Validates ownership, currency, and sufficient balance.",
				"Payments"));
		m.put(BankApiKeys.PAYMENTS_REVERSE, new BankApiOperationDoc(
				"Reverse a completed transfer",
				"Creates a compensating transfer for a COMPLETED transfer; subject to idempotency and business rules.",
				"Payments"));
	}

	private static void loans(Map<String, BankApiOperationDoc> m) {
		m.put(BankApiKeys.LOANS_ORIGINATE, new BankApiOperationDoc(
				"Apply for a loan",
				"Creates a loan in PENDING_APPROVAL with a generated amortization schedule.",
				"Loans"));
		m.put(BankApiKeys.LOANS_APPROVE, new BankApiOperationDoc(
				"Approve loan (demo)",
				"Approves a pending loan, creates loan bookkeeping account, disburses principal to checking, and publishes domain events.",
				"Loans"));
		m.put(BankApiKeys.LOANS_GET, new BankApiOperationDoc(
				"Get loan details",
				"Returns loan header, status, and repayment schedule lines for the borrower.",
				"Loans"));
		m.put(BankApiKeys.LOANS_PAY, new BankApiOperationDoc(
				"Pay one installment",
				"Marks an installment PAID, posts ledger movement, and may complete the loan when all installments are paid.",
				"Loans"));
	}

	private static void audit(Map<String, BankApiOperationDoc> m) {
		m.put(BankApiKeys.AUDIT_LIST, new BankApiOperationDoc(
				"Search audit records",
				"Filterable, paginated append-only audit log (requires audit:read).",
				"Audit"));
	}

	private static void notifications(Map<String, BankApiOperationDoc> m) {
		m.put(BankApiKeys.NOTIFICATIONS_RECORDS, new BankApiOperationDoc(
				"List notification log",
				"Paginated notification dispatch records for operations / support (requires notifications:read).",
				"Notifications"));
		m.put(BankApiKeys.NOTIFICATIONS_SUMMARY, new BankApiOperationDoc(
				"Notification counters",
				"Aggregate counts by status/channel for dashboards.",
				"Notifications"));
	}
}
