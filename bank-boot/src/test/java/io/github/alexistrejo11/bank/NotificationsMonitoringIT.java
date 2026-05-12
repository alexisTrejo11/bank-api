package io.github.alexistrejo11.bank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationsMonitoringIT {

	private static final ObjectMapper JSON = new ObjectMapper();
	private static final String ADMIN_ROLE_ID = "00000000-0000-0000-0000-0000000000a2";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("GET monitoring summary requires notifications:read")
	void summary_requires_permission() throws Exception {
		mockMvc.perform(get("/api/v1/notifications/monitoring/summary").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("GET monitoring summary returns 200 for notifications:read")
	void summary_ok_for_auditor() throws Exception {
		String email = "notif-it-" + UUID.randomUUID() + "@test.local";
		registerAndLogin(email, "Secretpass1!");
		grantAdminRole(email);
		String token = login(email, "Secretpass1!");

		mockMvc.perform(get("/api/v1/notifications/monitoring/summary")
						.accept(MediaType.APPLICATION_JSON)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
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
				UUID.fromString(ADMIN_ROLE_ID)
		);
	}
}
