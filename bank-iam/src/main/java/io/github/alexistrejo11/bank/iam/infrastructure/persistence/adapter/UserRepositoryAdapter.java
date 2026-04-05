package io.github.alexistrejo11.bank.iam.infrastructure.persistence.adapter;

import io.github.alexistrejo11.bank.iam.domain.model.User;
import io.github.alexistrejo11.bank.iam.domain.model.UserStatus;
import io.github.alexistrejo11.bank.iam.domain.port.out.UserRepository;
import io.github.alexistrejo11.bank.iam.infrastructure.persistence.entity.RoleEntity;
import io.github.alexistrejo11.bank.iam.infrastructure.persistence.entity.UserEntity;
import io.github.alexistrejo11.bank.iam.infrastructure.persistence.repository.RoleJpaRepository;
import io.github.alexistrejo11.bank.iam.infrastructure.persistence.repository.UserJpaRepository;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepository {

	private final UserJpaRepository userJpaRepository;
	private final RoleJpaRepository roleJpaRepository;

	public UserRepositoryAdapter(UserJpaRepository userJpaRepository, RoleJpaRepository roleJpaRepository) {
		this.userJpaRepository = userJpaRepository;
		this.roleJpaRepository = roleJpaRepository;
	}

	@Override
	public boolean existsByEmail(String email) {
		return userJpaRepository.existsByEmail(email);
	}

	@Override
	public void save(User user) {
		List<RoleEntity> roles = roleJpaRepository.findByNameIn(user.roleNames());
		Set<RoleEntity> roleSet = new HashSet<>(roles);
		Instant now = Instant.now();
		UserEntity entity = new UserEntity(
				user.id().value(),
				user.email(),
				user.passwordHash(),
				user.status(),
				now,
				now
		);
		entity.setRoles(roleSet);
		userJpaRepository.save(entity);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return userJpaRepository.findByEmailWithRolesAndPermissions(email).map(UserMapper::toDomain);
	}

	@Override
	public Optional<User> findById(UserId id) {
		return userJpaRepository.findByIdWithRolesAndPermissions(id.value()).map(UserMapper::toDomain);
	}
}
