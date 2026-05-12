package io.github.alexistrejo11.bank.iam.domain.repository;

import io.github.alexistrejo11.bank.iam.domain.model.User;
import io.github.alexistrejo11.bank.shared.shared_kernel.ids.UserId;
import java.util.Optional;

public interface UserRepository {

	boolean existsByEmail(String email);

	void save(User user);

	Optional<User> findByEmail(String email);

	Optional<User> findById(UserId id);
}
