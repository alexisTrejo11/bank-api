package io.github.alexistrejo11.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.alexistrejo11.bank.shared.shared_kernel.ids.LoanId;
import io.github.alexistrejo11.bank.shared.shared_kernel.event.LoanPaidOffEvent;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfigureMockMvc
class AuditModuleIT extends AbstractBankIntegrationTest {

	private static final ObjectMapper JSON = new ObjectMapper();
	private static final String ADMIN_ROLE_ID = "00000000-0000-0000-0000-0000000000a2";

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Autowired
	TransactionTemplate transactionTemplate;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("should persist audit row after BankDomainEvent transaction commits")
	void should_persist_audit_after_event_commit() {
		Long before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM audit_records", Long.class);
		transactionTemplate
				.executeWithoutResult(status -> eventPublisher.publishEvent(new LoanPaidOffEvent(LoanId.random())));
		Long after = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM audit_records", Long.class);
		assertThat(after).isEqualTo(before + 1);
	}

	@Test
	@DisplayName("should allow UPDATE on audit_records when append-only trigger is absent")
	void should_allow_update_on_audit_records_without_trigger() {
		transactionTemplate
				.executeWithoutResult(status -> eventPublisher.publishEvent(new LoanPaidOffEvent(LoanId.random())));
		UUID id = jdbcTemplate.queryForObject("SELECT id FROM audit_records ORDER BY created_at DESC LIMIT 1", UUID.class);
		int rows = jdbcTemplate.update("UPDATE audit_records SET entity_type = 'x' WHERE id = ?", id);
		assertThat(rows).isEqualTo(1);
	}

	@Test
	@DisplayName("should allow GET /api/v1/audit/records for audit:read")
	void should_allow_audit_query_for_auditor() throws Exception {
		String email = "audit-it-" + UUID.randomUUID() + "@test.local";
		registerAndLogin(email, "Secretpass1!");
		grantAdminRole(email);
		String token = login(email, "Secretpass1!");

		mockMvc.perform(get("/api/v1/audit/records").param("size", "5")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("should reject unauthenticated GET /api/v1/audit/records")
	void should_reject_unauthenticated_audit_query() throws Exception {
		mockMvc.perform(get("/api/v1/audit/records"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("should forbid GET /api/v1/audit/records when missing audit:read")
	void should_forbid_audit_query_without_audit_read() throws Exception {
		String token = registerAndLogin("audit-user-" + UUID.randomUUID() + "@test.local", "Secretpass1!");

		mockMvc.perform(get("/api/v1/audit/records")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	private String registerAndLogin(String email, String password) throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
				.andExpect(status().isCreated());

		return login(email, password);
	}

	private String login(String email, String password) throws Exception {
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode body = JSON.readTree(login.getResponse().getContentAsString());
		return body.path("data").path("accessToken").asText();
	}

	private void grantAdminRole(String email) {
		UUID userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", UUID.class, email);
		jdbcTemplate.update(
				"INSERT INTO user_roles (user_id, role_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
				userId,
				UUID.fromString(ADMIN_ROLE_ID));
	}
}
