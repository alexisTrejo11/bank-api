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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountsModuleIT {

	private static final ObjectMapper JSON = new ObjectMapper();

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("open account, read balance, read ledger using real auth")
	void accounts_happy_path() throws Exception {
		String email = "acc-it-" + UUID.randomUUID() + "@test.local";
		String token = registerAndLogin(email, "Secretpass1!");
		UUID accountId = openAccount(token, AccountType.CHECKING, "USD");

		MvcResult balance = mockMvc.perform(get("/api/v1/accounts/" + accountId + "/balance")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn();
		JsonNode balanceJson = JSON.readTree(balance.getResponse().getContentAsString()).path("data");
		assertThat(balanceJson.path("currency").asText()).isEqualTo("USD");
		assertThat(balanceJson.path("balance").asInt()).isZero();

		MvcResult ledger = mockMvc.perform(get("/api/v1/accounts/" + accountId + "/ledger")
				.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn();
		JsonNode ledgerJson = JSON.readTree(ledger.getResponse().getContentAsString()).path("data");
		assertThat(ledgerJson.path("content").isArray()).isTrue();
	}

	private String registerAndLogin(String email, String password) throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
				.andExpect(status().isCreated());

		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode body = JSON.readTree(login.getResponse().getContentAsString());
		return body.path("data").path("accessToken").asText();
	}

	private UUID openAccount(String bearer, AccountType type, String currency) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/accounts")
				.header("Authorization", "Bearer " + bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"type\":\"" + type.name() + "\",\"currency\":\"" + currency + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		return UUID
				.fromString(JSON.readTree(result.getResponse().getContentAsString()).path("data").path("accountId").asText());
	}
}
