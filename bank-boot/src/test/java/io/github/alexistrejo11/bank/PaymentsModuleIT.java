package io.github.alexistrejo11.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentsModuleIT {

	private static final ObjectMapper JSON = new ObjectMapper();

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("should reject transfer without Idempotency-Key")
	void should_require_idempotency_key() throws Exception {
		String token = registerAndLogin("pay-it-" + UUID.randomUUID() + "@test.local", "Secretpass1!");
		mockMvc.perform(post("/api/v1/payments/transfers")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"sourceAccountId":"%s","targetAccountId":"%s","amount":10,"currency":"USD"}
								""".formatted(UUID.randomUUID(), UUID.randomUUID())))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("should complete transfer between owned accounts and return 200")
	void should_transfer_between_accounts() throws Exception {
		String email = "pay-tr-" + UUID.randomUUID() + "@test.local";
		String token = registerAndLogin(email, "Secretpass1!");
		UUID a1 = openAccount(token, AccountType.CHECKING, "USD");
		UUID a2 = openAccount(token, AccountType.SAVINGS, "USD");
		seedCredit(a1, new BigDecimal("100.00"), "USD");
		UUID idem = UUID.randomUUID();

		MvcResult result = mockMvc.perform(post("/api/v1/payments/transfers")
						.header("Authorization", "Bearer " + token)
						.header("Idempotency-Key", idem.toString())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"sourceAccountId":"%s","targetAccountId":"%s","amount":25.00,"currency":"USD"}
								""".formatted(a1, a2)))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode body = JSON.readTree(result.getResponse().getContentAsString());
		assertThat(body.path("data").path("status").asText()).isEqualTo("COMPLETED");
		assertThat(body.path("data").path("amount").decimalValue()).isEqualByComparingTo(new BigDecimal("25.00"));
	}

	private String registerAndLogin(String email, String password) throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
				.andExpect(status().isOk());
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		JsonNode n = JSON.readTree(login.getResponse().getContentAsString());
		return n.path("data").path("accessToken").asText();
	}

	private void seedCredit(UUID accountId, BigDecimal amount, String currency) {
		jdbcTemplate.update(
				"""
						INSERT INTO ledger_entries (id, account_id, entry_type, amount, currency, reference_type, reference_id, created_at)
						VALUES (?, ?, 'CREDIT', ?, ?, 'TEST_SEED', ?, ?)
						""",
				UUID.randomUUID(), accountId, amount, currency, UUID.randomUUID(), Timestamp.from(Instant.now())
		);
	}

	private UUID openAccount(String bearer, AccountType type, String currency) throws Exception {
		MvcResult r = mockMvc.perform(post("/api/v1/accounts")
						.header("Authorization", "Bearer " + bearer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"type\":\"" + type.name() + "\",\"currency\":\"" + currency + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		return UUID.fromString(JSON.readTree(r.getResponse().getContentAsString()).path("data").path("accountId").asText());
	}
}
