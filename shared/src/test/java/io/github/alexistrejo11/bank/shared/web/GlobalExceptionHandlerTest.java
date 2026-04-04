package io.github.alexistrejo11.bank.shared.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import io.github.alexistrejo11.bank.shared.exception.BankException;
import io.github.alexistrejo11.bank.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@RestController
	static class TestApi {

		@GetMapping("/test/not-found")
		void notFound() {
			throw new ResourceNotFoundException("NOT_FOUND", "missing");
		}

		@GetMapping("/test/bank")
		void bank() {
			throw new BankException("BAD", "nope") { };
		}
	}

	@BeforeEach
	void setUp() {
		mockMvc = standaloneSetup(new TestApi()).setControllerAdvice(new GlobalExceptionHandler()).build();
	}

	@Test
	@DisplayName("should_map_ResourceNotFoundException_to_404")
	void should_map_ResourceNotFoundException_to_404() throws Exception {
		mockMvc.perform(get("/test/not-found"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("missing"))
				.andExpect(jsonPath("$.errorCode").value("NOT_FOUND"));
	}

	@Test
	@DisplayName("should_map_BankException_to_400")
	void should_map_BankException_to_400() throws Exception {
		mockMvc.perform(get("/test/bank"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("BAD"));
	}
}
