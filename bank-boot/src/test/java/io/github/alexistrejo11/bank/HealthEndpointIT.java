package io.github.alexistrejo11.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class HealthEndpointIT extends AbstractBankIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("GET /health is public without Authorization header")
	void health_withoutAuth() throws Exception {
		mockMvc.perform(get("/health"))
				.andExpect(status().isOk())
				.andExpect(content().string("OK"));
	}

	@Test
	@DisplayName("GET /health is public even with invalid Bearer token")
	void health_withInvalidBearer() throws Exception {
		mockMvc.perform(get("/health").header("Authorization", "Bearer not-a-jwt"))
				.andExpect(status().isOk())
				.andExpect(content().string("OK"));
	}

	@Test
	@DisplayName("GET /actuator/health is public without Authorization header")
	void actuatorHealth_withoutAuth() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("protected API without token is unauthorized")
	void protectedApi_withoutAuth() throws Exception {
		mockMvc.perform(post("/api/v1/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"type\":\"CHECKING\",\"currency\":\"USD\"}"))
				.andExpect(status().isForbidden());
	}
}
