package io.github.alexistrejo11.bank.shared.messaging;

/** Kafka topic names (v0.3.0 infra). */
public final class BankKafkaTopics {

	public static final String TRANSFERS = "bank.transfers";
	public static final String ACCOUNTS = "bank.accounts";
	public static final String LOANS = "bank.loans";
	public static final String NOTIFICATIONS = "bank.notifications";
	public static final String AUDIT = "bank.audit";
	public static final String DLQ = "bank.dlq";

	private BankKafkaTopics() {
	}
}
