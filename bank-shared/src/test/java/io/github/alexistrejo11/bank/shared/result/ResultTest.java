package io.github.alexistrejo11.bank.shared.result;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResultTest {

	@Test
	void success_is_success() {
		Result<String> r = Result.success("ok");
		assertThat(r.isSuccess()).isTrue();
		assertThat(r.isFailure()).isFalse();
		assertThat(((Result.Success<String>) r).value()).isEqualTo("ok");
	}

	@Test
	void failure_is_failure() {
		Result<String> r = Result.failure("CODE", "msg");
		assertThat(r.isFailure()).isTrue();
		assertThat(((Result.Failure<String>) r).code()).isEqualTo("CODE");
		assertThat(((Result.Failure<String>) r).message()).isEqualTo("msg");
	}
}
