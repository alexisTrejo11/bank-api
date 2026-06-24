package io.github.alexistrejo11.bank;

import io.github.alexistrejo11.bank.shared.shared_kernel.messaging.BankKafkaTopics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
		partitions = 1,
		topics = {
				BankKafkaTopics.TRANSFERS,
				BankKafkaTopics.ACCOUNTS,
				BankKafkaTopics.LOANS,
				BankKafkaTopics.NOTIFICATIONS,
				BankKafkaTopics.AUDIT,
				BankKafkaTopics.DLQ,
				"bank.notifications.dispatch",
				"bank.notifications.pipeline"
		})
public abstract class AbstractBankIntegrationTest {
}
