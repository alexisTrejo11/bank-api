package io.github.alexistrejo11.bank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexistrejo11.bank.accounts.domain.model.AccountType;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoansModuleIT {

	private static final ObjectMapper JSON = new ObjectMapper();

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("originate, approve, pay installment, reject duplicate pay")
	void loan_lifecycle() throws Exception {
		String email = "loan-it-" + UUID.randomUUID() + "@test.local";
		String token = registerAndLogin(email, "Secretpass1!");
		UUID checkingId = openAccount(token, AccountType.CHECKING, "USD");

		MvcResult orig = mockMvc.perform(post("/api/v1/loans")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"checkingAccountId":"%s","principal":500,"currency":"USD","monthlyInterestRate":0,"termMonths":1}
								""".formatted(checkingId)))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode loanJson = JSON.readTree(orig.getResponse().getContentAsString()).path("data");
		UUID loanId = UUID.fromString(loanJson.path("loanId").asText());
		assertThat(loanJson.path("status").asText()).isEqualTo("PENDING_APPROVAL");
		UUID repaymentId = UUID.fromString(loanJson.path("repayments").get(0).path("repaymentId").asText());

		mockMvc.perform(post("/api/v1/loans/" + loanId + "/approve")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/loans/" + loanId + "/repayments/" + repaymentId + "/pay")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/loans/" + loanId + "/repayments/" + repaymentId + "/pay")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isUnprocessableEntity());

		MvcResult detail = mockMvc.perform(get("/api/v1/loans/" + loanId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn();
		assertThat(JSON.readTree(detail.getResponse().getContentAsString()).path("data").path("status").asText())
				.isEqualTo("PAID_OFF");
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
