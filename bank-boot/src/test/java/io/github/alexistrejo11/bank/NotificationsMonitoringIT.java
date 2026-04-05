package io.github.alexistrejo11.bank;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationsMonitoringIT {

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("GET monitoring summary requires notifications:read")
	void summary_requires_permission() throws Exception {
		mockMvc.perform(get("/api/v1/notifications/monitoring/summary").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("GET monitoring summary returns 200 for notifications:read")
	void summary_ok_for_auditor() throws Exception {
		mockMvc.perform(get("/api/v1/notifications/monitoring/summary")
						.accept(MediaType.APPLICATION_JSON)
						.with(user("ops").authorities(new SimpleGrantedAuthority("notifications:read"))))
				.andExpect(status().isOk());
	}
}
