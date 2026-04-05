package io.github.alexistrejo11.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.alexistrejo11.bank.audit.infrastructure.persistence.repository.AuditRecordJpaRepository;
import io.github.alexistrejo11.bank.shared.event.TransferCompletedEvent;
import io.github.alexistrejo11.bank.shared.ids.AccountId;
import io.github.alexistrejo11.bank.shared.ids.TransferId;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditModuleIT {

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Autowired
	AuditRecordJpaRepository auditRecordRepository;

	@Autowired
	TransactionTemplate transactionTemplate;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("should persist audit row after BankDomainEvent transaction commits")
	void should_persist_audit_after_event_commit() {
		long before = auditRecordRepository.count();
		transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(
				new TransferCompletedEvent(
						TransferId.random(),
						AccountId.random(),
						AccountId.random(),
						new BigDecimal("42.50"),
						"MXN"
				)
		));
		assertThat(auditRecordRepository.count()).isEqualTo(before + 1);
	}

	@Test
	@DisplayName("should reject UPDATE on audit_records via database trigger")
	void should_reject_update_on_audit_records() {
		transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(
				new TransferCompletedEvent(
						TransferId.random(),
						AccountId.random(),
						AccountId.random(),
						BigDecimal.ONE,
						"USD"
				)
		));
		UUID id = auditRecordRepository.findAll().getFirst().getId();
		assertThatThrownBy(() -> jdbcTemplate.update("UPDATE audit_records SET entity_type = 'x' WHERE id = ?", id))
				.isInstanceOf(DataAccessException.class);
	}

	@Test
	@WithMockUser(authorities = "audit:read")
	@DisplayName("should allow GET /api/v1/audit/records for audit:read")
	void should_allow_audit_query_for_auditor() throws Exception {
		mockMvc.perform(get("/api/v1/audit/records").param("size", "5"))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("should reject unauthenticated GET /api/v1/audit/records")
	void should_reject_unauthenticated_audit_query() throws Exception {
		mockMvc.perform(get("/api/v1/audit/records"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(authorities = "accounts:read")
	@DisplayName("should forbid GET /api/v1/audit/records when missing audit:read")
	void should_forbid_audit_query_without_audit_read() throws Exception {
		mockMvc.perform(get("/api/v1/audit/records"))
				.andExpect(status().isForbidden());
	}
}
