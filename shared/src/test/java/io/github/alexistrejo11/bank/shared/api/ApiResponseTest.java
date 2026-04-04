package io.github.alexistrejo11.bank.shared.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

	@Test
	void success_has_no_errors() {
		ApiResponse<String> r = ApiResponse.success("data");
		assertThat(r.isSuccess()).isTrue();
		assertThat(r.data()).isEqualTo("data");
		assertThat(r.errors()).isEmpty();
	}

	@Test
	void failure_has_errors() {
		ApiResponse<String> r = ApiResponse.failure("E", "bad");
		assertThat(r.isSuccess()).isFalse();
		assertThat(r.errors()).hasSize(1);
		assertThat(r.errors().getFirst().code()).isEqualTo("E");
	}
}
