package io.github.alexistrejo11.bank.iam.infrastructure.persistence.adapter;

import io.github.alexistrejo11.bank.iam.domain.model.User;
import io.github.alexistrejo11.bank.iam.infrastructure.persistence.entity.PermissionEntity;
import io.github.alexistrejo11.bank.iam.infrastructure.persistence.entity.RoleEntity;
import io.github.alexistrejo11.bank.iam.infrastructure.persistence.entity.UserEntity;
import io.github.alexistrejo11.bank.shared.ids.UserId;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserMapper {

	private UserMapper() {
	}

	public static User toDomain(UserEntity entity) {
		Set<String> roleNames = entity.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet());
		Set<String> permissionNames = new HashSet<>();
		for (RoleEntity role : entity.getRoles()) {
			for (PermissionEntity p : role.getPermissions()) {
				permissionNames.add(p.getName());
			}
		}
		return new User(
				UserId.of(entity.getId()),
				entity.getEmail(),
				entity.getPasswordHash(),
				entity.getStatus(),
				roleNames,
				permissionNames
		);
	}
}
