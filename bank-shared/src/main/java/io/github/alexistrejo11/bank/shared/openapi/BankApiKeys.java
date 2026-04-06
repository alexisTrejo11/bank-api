package io.github.alexistrejo11.bank.shared.openapi;

/**
 * Stable keys for {@link BankApiOperation} — one place to rename or search documentation.
 */
public final class BankApiKeys {

	public static final String AUTH_REGISTER = "auth.register";
	public static final String AUTH_LOGIN = "auth.login";
	public static final String AUTH_REFRESH = "auth.refresh";
	public static final String AUTH_LOGOUT = "auth.logout";
	public static final String AUTH_ME = "auth.me";

	public static final String ACCOUNTS_OPEN = "accounts.open";
	public static final String ACCOUNTS_BALANCE = "accounts.balance";
	public static final String ACCOUNTS_LEDGER = "accounts.ledger";

	public static final String PAYMENTS_TRANSFER = "payments.transfer";
	public static final String PAYMENTS_REVERSE = "payments.reverse";

	public static final String LOANS_ORIGINATE = "loans.originate";
	public static final String LOANS_APPROVE = "loans.approve";
	public static final String LOANS_GET = "loans.get";
	public static final String LOANS_PAY = "loans.pay";

	public static final String AUDIT_LIST = "audit.list";

	public static final String NOTIFICATIONS_RECORDS = "notifications.records";
	public static final String NOTIFICATIONS_SUMMARY = "notifications.summary";

	private BankApiKeys() {
	}
}
